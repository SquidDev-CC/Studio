package squidev.ccstudio.__ignore;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.core.apis.wrapper.APIWrapper;

/**
 * For checking invoking functions
 */
public class Invoke extends APIWrapper<Invoke.SubThing> {
	protected static final String[][] METHOD_NAMES = {{"noArgsNoReturn"}, {"twoArgsOneReturn"}, {"noArgsLuaReturn"}, {"varArgsLuaReturn"}};
	public Invoke(SubThing inst) {
		super(inst);
		methodNames = METHOD_NAMES;
	}

	public Varargs invoke(Varargs args, int index) {
		switch(index) {
			case 0:
				instance.noArgsNoReturn();
				return LuaValue.NONE;
			case 1:
				if(args.narg() < 2 || !args.arg(1).isnumber() || !args.arg(2).isnumber()) {
					throw new LuaError("Expected number, number");
				}

				return LuaValue.valueOf(instance.twoArgsOneReturn(args.arg(1).todouble(), args.arg(2).todouble()));
			case 2:
				return instance.noArgsLuaReturn();
			case 3:
				return instance.varArgsLuaReturn(args);
		}

		return LuaValue.NONE;
	}

	public static class SubThing {
		public void noArgsNoReturn() { }
		public double twoArgsOneReturn(double a, double b) { return 0; }

		public LuaValue noArgsLuaReturn() { return LuaValue.NONE; }

		public LuaValue varArgsLuaReturn(Varargs args) { return LuaValue.NONE; }
	}
}
