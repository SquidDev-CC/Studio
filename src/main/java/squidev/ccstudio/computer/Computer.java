package squidev.ccstudio.computer;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;
import squidev.ccstudio.computer.api.BitAPI;
import squidev.ccstudio.computer.api.OSAPI;
import squidev.ccstudio.computer.api.RedstoneAPI;
import squidev.ccstudio.computer.api.TerminalAPI;
import squidev.ccstudio.core.Config;
import squidev.ccstudio.core.apis.CCAPI;
import squidev.ccstudio.core.apis.wrapper.APIClassLoader;
import squidev.ccstudio.core.apis.wrapper.LuaAPI;
import squidev.ccstudio.core.utils.FileSystemUtilities;
import squidev.ccstudio.output.IOutput;
import squidev.ccstudio.output.terminal.TerminalOutput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles computer
 */
public class Computer {
	public static final APIClassLoader loader = new APIClassLoader();
	public static final String[] SIDE_NAMES = {"bottom", "top", "back", "front", "right", "left"};
	public static final LuaValue[] SIDE_VALUES;
	public static final Map<String, Integer> SIDE_MAP;

	static {
		String[] names = SIDE_NAMES;
		int length = names.length;

		// Convert the names to values to make everything easier later on
		LuaValue[] values = new LuaValue[length];
		SIDE_VALUES = values;

		// And add it to a map to
		Map<String, Integer> map = new HashMap<>();
		SIDE_MAP = map;

		for (int i = 0; i < length; i++) {
			String name = names[i];
			values[i] = LuaValue.valueOf(name);
			map.put(name, i);
		}
	}

	public final Config config;
	public final ComputerEnvironment environment;
	/**
	 * The queue of items to resume with
	 */
	public final Queue<Runnable> events = new LinkedBlockingQueue<>(256);
	/**
	 * The event to filter on. Terminate events will still get through
	 */
	public String filter = null;
	protected IOutput output;
	protected List<CCAPI> apis = new ArrayList<>();
	/**
	 * The global environment to run in
	 */
	protected LuaValue globals;
	/**
	 * The main routine that code is executed in
	 */
	protected LuaThread mainCoroutine;
	/**
	 * The thread code is run on
	 */
	protected Thread mainThread;
	// The coroutine API
	protected LuaValue coroutineCreate;
	protected LuaValue coroutineYield;
	protected LuaValue coroutineResume;
	/**
	 * Triggers a LuaError inside the computer
	 */
	protected String softAbort = null;

	/**
	 * An error message that should stop the entire computer
	 */
	protected String hardAbort = null;

	protected FileSystem fileSystem;
	protected IWritableMount rootMount;
	protected IMount romMount;

	public Computer(Config config) {
		output = new TerminalOutput();

		this.config = config;
		environment = new ComputerEnvironment();

		// Load APIs
		addAPI(new OSAPI(this));
		addAPI(new RedstoneAPI(environment));
		addAPI(new BitAPI());
		addAPI(new TerminalAPI(this.output));
	}

	/**
	 * Add an API
	 *
	 * @param api The API to add
	 */
	public void addAPI(CCAPI api) {
		if (api == null) throw new IllegalArgumentException("API cannot be null");
		apis.add(api);
	}

	/**
	 * Add an API using the LuaAPI annotations
	 *
	 * @param objectInstance The instance to create the API from
	 * @see squidev.ccstudio.core.apis.wrapper.LuaAPI
	 */
	public void addAPI(Object objectInstance) {
		if (!objectInstance.getClass().isAnnotationPresent(LuaAPI.class)) {
			throw new IllegalArgumentException("Object must have API annotation");
		}

		addAPI(loader.makeInstance(objectInstance));
	}

	/**
	 * Setup the computer, generating globals and injecting APIs
	 */
	public void setup() {
		globals = JsePlatform.debugGlobals();

		// Sadly this isn't a local thing we can customise
		if (config.useLuaJC) LuaJC.install();

		LuaValue coroutineLib = globals.get("coroutine");
		if (config.coroutineHookCount > 0) {
			final LuaValue coroutineCreate = coroutineLib.get("create");

			LuaValue setHook = globals.get("debug").get("setHook");

			// We need to insert a new version of the Coroutine library
			coroutineLib.set("create", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					LuaValue coroutine = coroutineCreate.call(arg).checkthread();

					// Every <n> instructions:
					setHook.invoke(new LuaValue[]{
							coroutine,
							new ZeroArgFunction() {
								@Override
								public LuaValue call() {
									// Check if the computer should abort, if so then yield this co-routine
									if (Computer.this.hardAbort != null) {
										LuaThread.yield(LuaValue.NIL);
									}
									return LuaValue.NIL;
								}
							}, LuaValue.NIL, LuaValue.valueOf(config.coroutineHookCount)
					});

					return coroutine;
				}
			});
		}

		coroutineCreate = coroutineLib.get("create");
		coroutineYield = coroutineLib.get("yield");
		coroutineResume = coroutineLib.get("resume");

		// Load the APIs
		for (CCAPI api : apis) {
			if (api != null) {
				// Set environment and bind to the environment
				api.setup(this, globals);
				api.bind();
			}
		}

		// Clear all the blacklisted globals
		LuaValue nil = LuaValue.NIL;
		for (String global : config.blacklist) {
			globals.set(global, nil);
		}
	}

	/**
	 * Load the bios with the default bios
	 */
	public void loadBios() {
		// TODO: Load this from config
		loadBios(FileSystemUtilities.biosStream());
	}

	public void loadBios(String bios) {
		loadBios(new ByteArrayInputStream(bios.getBytes()));
	}

	/**
	 * Load the startup script
	 *
	 * @param bios The InputStream to read from
	 */
	public void loadBios(InputStream bios) {
		try {
			// Load the stream
			Varargs program = BaseLib.loadStream(bios, "bios");
			if (!program.arg1().toboolean()) throw new LuaError("Cannot load bios: " + program.arg(2).tostring());

			// And execute in a new thread
			mainCoroutine = (LuaThread) this.coroutineCreate.call(program.arg1());
		} catch (LuaError e) {
			if (this.mainCoroutine != null) {
				mainCoroutine.abandon();
				mainCoroutine = null;
			}
		}
	}

	protected void startThread() {
		mainThread = new Thread(new ComputerThread(this));
	}

	/**
	 * Resume the thread with arguments
	 *
	 * @param args The args to resume with
	 */
	public void resume(Varargs args) {
		try {
			Varargs result = coroutineResume.invoke(args);

			// If we have some sort of error message, stop everything
			if (hardAbort != null) throw new LuaError(hardAbort);

			if (!result.arg(1).checkboolean()) {
				throw new LuaError(result.arg(2).tojstring());
			}

			LuaValue filter = result.arg(2);
			if (filter.isstring()) {
				this.filter = filter.tojstring();
			} else {
				this.filter = null;
			}

			if (mainCoroutine.getStatus().equals("dead")) {
				mainCoroutine = null;
			}
		} catch (LuaError e) {
			mainCoroutine.abandon();
			mainCoroutine = null;

			// TODO: We should push a notification
		} finally {
			hardAbort = null;
			softAbort = null;
		}
	}

	/**
	 * Resume the thread with no arguments
	 */
	public void resume() {
		resume(LuaValue.NONE);
	}

	/**
	 * Used to trigger a soft abort
	 * This should be called on **every** API function call.
	 */
	public void tryAbort() {
		String abort = softAbort;
		if (abort != null) {
			softAbort = null;
			hardAbort = null;
			throw new LuaError(abort);
		}
	}

	/**
	 * Add a {@see ComputerEvent}
	 *
	 * @param name The name of the event
	 * @param args The arguments to pass
	 */
	public void queueEvent(String name, Varargs args) {
		events.add(new ComputerEvent(this, name, args));
	}

	/**
	 * Add a {@see ComputerEvent}
	 *
	 * @param args The arguments to pass, with the name as the first argument
	 */
	public void queueEvent(Varargs args) {
		events.add(new ComputerEvent(this, args));
	}

	/**
	 * Get the mount that is used for writing
	 *
	 * @return The writing mount
	 */
	public IWritableMount getRootMount() {
		// TODO: This should be possible to change
		if (rootMount == null) {
			try {
				rootMount = new FileMount(
						new File(config.computerDirectory, Integer.toString(environment.id)),
						config.computerSpaceLimit
				);
			} catch (Exception ignored) {
			}

		}
		return rootMount;
	}

	/**
	 * Load the filesystem
	 *
	 * @return Success on creating it
	 */
	public boolean createFilesystem() {
		try {
			this.fileSystem = new FileSystem("hdd", this.getRootMount());

			// TODO: This should be possible to change
			if (romMount == null) romMount = FileSystemUtilities.getJarRomMount();

			if (romMount != null) {
				this.fileSystem.mount("rom", "rom", romMount);
				return true;
			} else {
				return false;
			}
		} catch (FileSystemException e) {
			return false;
		}
	}
}
