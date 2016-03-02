package org.squiddev.studio.modifications.lua;

import dan200.computercraft.api.lua.LuaException;
import org.squiddev.studio.api.lua.IArguments;

public final class ArgumentExtensions {
	private ArgumentExtensions() {
	}

	public static double optionalNumber(IArguments args, int index, double def) throws LuaException {
		if (index >= args.size()) return def;
		return args.getNumber(index);
	}

	public static int optionalInt(IArguments args, int index, int def) throws LuaException {
		if (index >= args.size()) return def;
		return (int) args.getNumber(index);
	}
}
