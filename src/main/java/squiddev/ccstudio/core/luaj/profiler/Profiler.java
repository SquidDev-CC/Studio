package squiddev.ccstudio.core.luaj.profiler;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Profiles Lua files
 */
public class Profiler extends VarArgFunction {
	protected LuaFunction function;
	protected LuaValue env;
	protected LuaTable debugTable;
	protected DebugLib debug;

	protected Map<LuaFunction, Integer> calls = new HashMap<>();

	public Profiler(LuaFunction function) {
		this.function = function;
		env = function.getfenv();

		name = "Profiler";

		debugTable = (LuaTable) env.get("debug");
		DebugLib debug = this.debug = (DebugLib) debugTable.get("debug");

		DebugLib.DebugState current = DebugLib.getDebugState(LuaThread.getRunning());
		current.sethook(new VarArgFunction() {
			String indent;

			{
				name = "ProfilerHook";
				indent = "";
			}

			@Override
			public Varargs invoke(Varargs varargs) {
				DebugLib.DebugInfo info = DebugLib.getDebugState().getDebugInfo(1);
				LuaTable general = debugTable.get("getinfo").invoke(LuaValue.valueOf(2)).checktable(1);
				switch (varargs.arg1().toString()) {
					case "call":
						System.out.println(indent + general.get("name") + " " + info.sourceline());
						indent += "\t";
						break;
					case "line":
						//System.out.println("\t" + varargs.arg(2).toString());
						break;
					case "return":
						if (indent.length() > 0) indent = indent.substring(1);
						break;
					default:
						System.out.println("Unknown " + varargs.arg1().toString());
						break;
				}

				return LuaValue.NONE;
			}


		}, true, true, true, 0);
	}

	@Override
	public Varargs onInvoke(Varargs varargs) {
		return function.invoke(varargs);
	}
}
