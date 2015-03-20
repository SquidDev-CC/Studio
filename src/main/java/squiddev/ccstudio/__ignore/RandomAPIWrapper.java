package squiddev.ccstudio.__ignore;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * For checking invoking functions
 */
public class RandomAPIWrapper {
	private static final String[] NAMES = {"Random"};
	protected final RandomAPI instance = new RandomAPI();

	public RandomAPIWrapper(RandomAPI inst) {
		// super(inst);
	}

	public class RandomAPI_noArgsNoReturn extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			instance.noArgsNoReturn();
			return LuaValue.NONE;
		}
	}

	public class RandomAPI_twoArgsOneReturn extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
			if (!luaValue.isnumber() || luaValue1.isnumber()) {
				throw new LuaError("Expected number, number");
			}

			return LuaValue.valueOf(instance.twoArgsOneReturn(luaValue.todouble(), luaValue1.todouble()));
		}
	}

	public class RandomAPI_noArgsLuaReturn extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return instance.noArgsLuaReturn();
		}
	}

	public class RandomAPI_varArgsLuaReturn extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			return instance.varArgsLuaReturn(args);
		}
	}

	public class RandomAPI_strictArgOneReturn extends OneArgFunction {

		@Override
		public LuaValue call(LuaValue luaValue) {
			if (!(luaValue instanceof LuaNumber)) {
				throw new LuaError("Expected number, number");
			}

			return LuaValue.valueOf(instance.strictArgOneReturn((LuaNumber) luaValue));
		}
	}

	public String[] getNames() {
		return NAMES;
	}


	protected LuaTable createTable() {
		LuaTable table = new LuaTable();
		table.set("noArgsNoReturn", new RandomAPI_noArgsNoReturn());
		table.set("twoArgsOneReturn", new RandomAPI_twoArgsOneReturn());
		table.set("noArgsLuaReturn", new RandomAPI_noArgsLuaReturn());
		table.set("varArgsLuaReturn", new RandomAPI_varArgsLuaReturn());

		return table;
	}
}
