package org.squiddev.studio.modifications.lua;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import org.squiddev.studio.api.lua.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LuaEnvironment implements ILuaEnvironment {
	private final ILuaTask sleepTask = new ILuaTask() {
		@Override
		public Object[] execute() throws LuaException {
			return null;
		}
	};
	/**
	 * The instance of the Lua environment - this exists as ASM is easier this way
	 */
	public static final LuaEnvironment instance = new LuaEnvironment();

	private final Set<ILuaAPIFactory> providers = new HashSet<ILuaAPIFactory>();

	private LuaEnvironment() {
	}

	@Override
	public void registerAPI(ILuaAPIFactory factory) {
		if (factory == null) throw new IllegalArgumentException("factory cannot be null");
		providers.add(factory);
	}

	public static void inject(Computer computer) {
		if (instance.providers.size() == 0) return;

		IComputerAccess access = new ComputerAccess(computer);
		for (ILuaAPIFactory factory : instance.providers) {
			org.squiddev.studio.api.lua.ILuaAPI api = factory.create(access);
			if (api != null) {
				computer.addAPI(new LuaAPI(api, factory));
			}
		}
	}

	private static class LuaAPI implements ILuaAPI, ILuaObjectWithArguments, IExtendedLuaObject {
		private final org.squiddev.studio.api.lua.ILuaAPI api;
		private final ILuaAPIFactory factory;

		private LuaAPI(org.squiddev.studio.api.lua.ILuaAPI api, ILuaAPIFactory factory) {
			this.api = api;
			this.factory = factory;
		}

		@Override
		public String[] getNames() {
			return factory.getNames();
		}

		@Override
		public void startup() {
			api.startup();
		}

		@Override
		public void advance(double v) {
			api.advance(v);
		}

		@Override
		public void shutdown() {
			api.shutdown();
		}

		@Override
		public String[] getMethodNames() {
			return api.getMethodNames();
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
			return api.callMethod(context, method, args);
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, IArguments arguments) throws LuaException, InterruptedException {
			return ArgumentDelegator.delegateLuaObject(api, context, method, arguments);
		}

		@Override
		public Map<Object, Object> getAdditionalData() {
			return api instanceof IExtendedLuaObject ? ((IExtendedLuaObject) api).getAdditionalData() : Collections.emptyMap();
		}
	}

	private static final class ComputerAccess implements IComputerAccess {
		private final Computer computer;
		private final IAPIEnvironment environment;
		private final Set<String> mounts = new HashSet<String>();
		private FileSystem fs;

		private ComputerAccess(Computer computer) {
			this.computer = computer;
			this.environment = computer.getAPIEnvironment();
		}

		private FileSystem getFs() {
			if (fs == null) fs = environment.getFileSystem();
			return fs;
		}


		private String findFreeLocation(String desiredLoc) {
			try {
				synchronized (getFs()) {
					return !fs.exists(desiredLoc) ? desiredLoc : null;
				}
			} catch (FileSystemException ignored) {
				return null;
			}
		}

		@Override
		public String mount(String desiredLoc, IMount mount) {
			return this.mount(desiredLoc, mount, desiredLoc);
		}

		@Override
		public synchronized String mount(String desiredLoc, IMount mount, String driveName) {
			synchronized (getFs()) {
				String location = findFreeLocation(desiredLoc);
				if (location != null) {
					try {
						getFs().mount(driveName, location, mount);
					} catch (FileSystemException ignored) {
					}

					mounts.add(location);
				}

				return location;
			}
		}

		@Override
		public String mountWritable(String desiredLoc, IWritableMount mount) {
			return this.mountWritable(desiredLoc, mount, desiredLoc);
		}

		@Override
		public synchronized String mountWritable(String desiredLoc, IWritableMount mount, String driveName) {
			synchronized (getFs()) {
				String location = findFreeLocation(desiredLoc);
				if (location != null) {
					try {
						getFs().mountWritable(driveName, location, mount);
					} catch (FileSystemException ignored) {
					}

					mounts.add(location);
				}

				return location;
			}
		}

		@Override
		public synchronized void unmount(String location) {
			if (location != null) {
				if (!mounts.contains(location)) {
					throw new RuntimeException("You didn\'t mount this location");
				}

				getFs().unmount(location);
				mounts.remove(location);
			}
		}

		@Override
		public int getID() {
			return environment.getComputerID();
		}

		@Override
		public void queueEvent(String s, Object[] objects) {
			environment.queueEvent(s, objects);
		}

		@Override
		public String getAttachmentName() {
			return environment.getLabel();
		}
	}
}
