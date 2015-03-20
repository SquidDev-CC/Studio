package squiddev.ccstudio.__ignore;

import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;

/**
 * A random API that does things
 */
@LuaAPI("Random")
public class RandomAPI {
	@LuaFunction
	public void noArgsNoReturn() {
	}

	@LuaFunction
	public double twoArgsOneReturn(double a, double b) {
		return 0;
	}

	@LuaFunction
	public LuaValue noArgsLuaReturn() {
		return LuaValue.NONE;
	}

	@LuaFunction
	public LuaValue varArgsLuaReturn(Varargs args) {
		return LuaValue.NONE;
	}

	public double strictArgOneReturn(LuaNumber number) {
		return number.todouble() * 4;
	}
}
