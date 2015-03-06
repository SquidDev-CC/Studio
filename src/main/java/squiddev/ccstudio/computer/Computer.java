package squiddev.ccstudio.computer;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJCRewrite;
import squiddev.ccstudio.computer.api.*;
import squiddev.ccstudio.core.Config;
import squiddev.ccstudio.core.apis.CCAPI;
import squiddev.ccstudio.core.apis.wrapper.APIClassLoader;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.utils.FileSystemUtilities;
import squiddev.ccstudio.output.IOutput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
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
	public final BlockingQueue<Runnable> events = new LinkedBlockingQueue<>(256);
	public final Queue<String> messages = new LinkedList<>();

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
	protected ComputerThread mainThread = new ComputerThread(this);

	// The coroutine API
	protected LuaValue coroutineCreate;
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

	public Computer(Config config, IOutput output) {
		this.output = output;

		this.config = config;
		environment = new ComputerEnvironment();

		// Load APIs
		addAPI(new OSAPI(this));
		addAPI(new RedstoneAPI(environment));
		addAPI(new BitAPI());
		addAPI(new TerminalAPI(output));
		addAPI(new FileSystemAPI(this));
		addAPI(new PeripheralAPI());
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
	 * @see squiddev.ccstudio.core.apis.wrapper.LuaAPI
	 */
	public void addAPI(Object objectInstance) {
		if (!objectInstance.getClass().isAnnotationPresent(LuaAPI.class)) {
			throw new IllegalArgumentException("Object must have API annotation");
		}

		addAPI(loader.makeInstance(objectInstance));
	}

	/**
	 * Setup the computer, generating globals and injecting APIs
	 * Also create the file system
	 */
	protected void setup() {
		if (!createFilesystem()) throw new RuntimeException("Cannot create filesystem");

		globals = JsePlatform.debugGlobals();

		// Sadly this isn't a local thing we can't really customise this
		if (config.useLuaJC) {
			LuaJCRewrite.install();
		}

		LuaValue coroutineLib = globals.get("coroutine");
		if (config.coroutineHookCount > 0) {
			final LuaValue coroutineCreate = coroutineLib.get("create");

			final LuaValue setHook = globals.get("debug").get("sethook");

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
									LuaThread.yield(LuaValue.NONE);
								}
								return LuaValue.NONE;
							}
						}, LuaValue.NIL, LuaValue.valueOf(config.coroutineHookCount)
					});

					return coroutine;
				}
			});
		}

		coroutineCreate = coroutineLib.get("create");

		// Clear all the blacklisted globals
		LuaValue nil = LuaValue.NIL;
		for (String global : config.blacklist) {
			globals.set(global, nil);
		}

		// Load the APIs
		for (CCAPI api : apis) {
			if (api != null) {
				// Set environment and bind to the environment
				api.setup(this, globals);
				api.bind();
			}
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
			LuaFunction start = (LuaFunction) program.arg1();
			start.setfenv(globals);

			// And execute in a new thread
			mainCoroutine = (LuaThread) this.coroutineCreate.call(start);
		} catch (LuaError e) {
			if (this.mainCoroutine != null) {
				mainCoroutine.abandon();
				mainCoroutine = null;
			}
			System.out.println("Cannot load bios");
			e.printStackTrace();
		}
	}

	/**
	 * Start the computer thread
	 */
	public void start() {
		synchronized (events) {
			if (mainThread.getState() != ComputerThread.State.STOPPED) {
				throw new IllegalStateException("Computer is running");
			}
			events.clear();
			events.add(new Runnable() {
				@Override
				public void run() {
					setup();
					loadBios();
					mainCoroutine.resume(LuaValue.NONE);
				}
			});
			mainThread.start();
		}
	}

	/**
	 * Shutdown the computer
	 *
	 * @param force Force a shutdown by terminating the thread
	 */
	public void shutdown(boolean force) {
		synchronized (events) {
			if (mainThread.getState() == ComputerThread.State.STOPPED) {
				throw new IllegalStateException("Computer is not running");
			}

			Runnable task = new Runnable() {
				@Override
				public void run() {
					if (fileSystem != null) {
						fileSystem.unload();
					}

					if (mainCoroutine != null) {
						mainCoroutine.abandon();
						mainCoroutine = null;
					}

					mainThread.stop(force);

					output.clear();
					output.setBlink(false);
				}
			};

			if (force) {
				mainThread.stop(true);
				task.run();
			} else {
				events.add(task);
			}
		}
	}

	/**
	 * Shutdown the computer
	 */
	public void shutdown() {
		shutdown(false);
	}

	/**
	 * Resume the thread with arguments
	 *
	 * @param args The args to resume with
	 */
	public void resume(Varargs args) {
		try {
			Varargs result = mainCoroutine.resume(args);

			if (mainCoroutine.getStatus().equals("dead")) {
				mainCoroutine = null;
			}

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
		} catch (LuaError e) {
			e.printStackTrace();

			mainThread.stop();
			if (mainCoroutine != null) {
				mainCoroutine.abandon();
				mainCoroutine = null;
			}
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
	 * Set a hard abort message
	 *
	 * @param message The message to use
	 */
	public void hardAbort(String message) {
		hardAbort = message;
	}

	/**
	 * Set a soft abort message
	 */
	public void softAbort(String message) {
		softAbort = message;
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

	public FileSystem getFileSystem() {
		if (fileSystem == null) createFilesystem();
		return fileSystem;
	}

	public CCAPI createLuaObject(Object object) {
		CCAPI api = loader.makeInstance(object);
		api.setup(this, globals);
		return api;
	}

	public boolean isAlive() {
		return mainThread != null && mainThread.getState() != ComputerThread.State.STOPPED;
	}
}
