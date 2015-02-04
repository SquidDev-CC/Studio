package squidev.ccstudio.__ignore;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * Compiled lua class of 'return "hello"'
 */
public class chunk_LuaCompiled extends VarArgFunction {
	final static LuaValue k0;

	public final Varargs onInvoke(Varargs args) {
		return k0;
	}

	static {
		k0 = LuaString.valueOf("hello");
	}
}
