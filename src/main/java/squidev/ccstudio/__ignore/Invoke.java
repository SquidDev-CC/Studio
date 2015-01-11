package squidev.ccstudio.__ignore;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.core.apis.CCAPI;

/**
 * squidev.ccstudio.__ignore (CCStudio.Java
 */
public class Invoke extends CCAPI<Invoke.SubThing>{
	protected static final String[] METHOD_NAMES = {"hello", "goodbye"};
	public Invoke(SubThing inst) {
		super(inst);
		methodNames = METHOD_NAMES;
	}

	public Varargs invoke(Varargs args) {
		switch(opcode) {
			case 0:
				instance.sayHello();
				return LuaValue.NONE;
			case 1:
				if(args.narg() < 1) throw new LuaError("Expected double");

				LuaValue val = args.arg(0);
				if(!val.isnumber()) throw new LuaError("Expected double");
				double var_0 = val.todouble();

				double result = instance.sayGoodbye(var_0);
				return LuaValue.valueOf(result);
		}

		return LuaValue.NONE;
	}

	public static class SubThing {
		public void sayHello() { }
		public double sayGoodbye(double a) { return 0; }
	}
}
