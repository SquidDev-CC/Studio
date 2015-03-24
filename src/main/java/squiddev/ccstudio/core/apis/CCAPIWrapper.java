package squiddev.ccstudio.core.apis;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * A wrapper for using one `invoke` method for
 * multiple lua methods
 */
public abstract class CCAPIWrapper extends CCAPI {
	protected String[][] methodNames = null;
	protected String[] names = null;

	/**
	 * Returns the variables this API should be stored in
	 *
	 * @return List of variable names
	 */
	public String[] getNames() {
		return names;
	}

	public abstract Varargs invoke(Varargs args, int index);

	/**
	 * Convert this to a Lua table
	 *
	 * @return The API object
	 */
	protected LuaTable createTable() {
		LuaTable table = new LuaTable();
		try {
			for (int i = 0, n = methodNames.length; i < n; i++) {
				// Allow multiple names
				for (String name : methodNames[i]) {
					// Each function should be a different object, even if it is identical.
					LuaFunction f = new InvokeFunction(i);
					f.setfenv(env);
					table.set(name, f);
				}
			}
		} catch (Exception e) {
			throw new LuaError("Bind failed: " + e);
		}
		return table;
	}

	protected class InvokeFunction extends VarArgFunction {
		public final int index;

		public InvokeFunction(int index) {
			this.index = index;
		}

		/**
		 * We override the invoke function (not onInvoke) to prevent changing the stack
		 */
		@Override
		public Varargs invoke(Varargs varargs) {
			computer.tryAbort();
			return CCAPIWrapper.this.invoke(varargs, index);
		}
	}
}
