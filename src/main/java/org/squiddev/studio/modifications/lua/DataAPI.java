package org.squiddev.studio.modifications.lua;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import org.squiddev.studio.api.lua.IArguments;
import org.squiddev.studio.api.lua.ILuaAPI;
import org.squiddev.studio.api.lua.ILuaAPIFactory;
import org.squiddev.studio.api.lua.ILuaObjectWithArguments;
import org.squiddev.studio.modifications.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * Adds inflate/deflate APIs
 */
public class DataAPI implements ILuaAPI, ILuaObjectWithArguments, ILuaAPIFactory {
	@Override
	public void startup() {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public void advance(double timestep) {
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"inflate", "deflate"};
	}

	@Override
	public ILuaAPI create(IComputerAccess computer) {
		return Config.APIs.Data.enabled ? this : null;
	}

	@Override
	public String[] getNames() {
		return new String[]{"data"};
	}


	public byte[] getStringBytes(Object[] args) throws LuaException {
		if (args.length == 0 || !(args[0] instanceof String)) throw new LuaException("Expected string");

		return LuaConverter.toBytes((String) args[0]);
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return inflate(getStringBytes(args));

			case 1:
				return deflate(getStringBytes(args));
		}

		return null;
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, IArguments args) throws LuaException, InterruptedException {
		switch (method) {
			case 0:
				return inflate(args.getStringBytes(0));

			case 1:
				return deflate(args.getStringBytes(0));
		}

		return null;
	}

	private Object[] inflate(byte[] data) throws LuaException {
		if (data.length >= Config.APIs.Data.limit) throw new LuaException("Data is too long");

		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		InflaterOutputStream inos = new InflaterOutputStream(baos);
		try {
			inos.write(data);
			inos.finish();
		} catch (IOException e) {
			throw LuaHelpers.rewriteException(e, "Inflating error");
		}

		return new Object[]{baos.toByteArray()};
	}

	private Object[] deflate(byte[] data) throws LuaException {
		if (data.length >= Config.APIs.Data.limit) throw new LuaException("Data is too long");

		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		DeflaterOutputStream inos = new DeflaterOutputStream(baos);
		try {
			inos.write(data);
			inos.finish();
		} catch (IOException e) {
			throw LuaHelpers.rewriteException(e, "Deflating error");
		}

		return new Object[]{baos.toByteArray()};
	}
}
