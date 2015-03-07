package squiddev.ccstudio.core.luaj;

import org.luaj.vm2.LuaValue;

/**
 * Helper conversion
 */
public class Conversion {
	public static LuaValue valueOf(String name) {
		if (name == null) return LuaValue.NIL;
		return LuaValue.valueOf(name);
	}
}
