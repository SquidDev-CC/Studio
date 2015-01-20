package squidev.ccstudio.core.apis;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import squidev.ccstudio.computer.Computer;

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
				final int index = i;
				LuaFunction f = new VarArgFunction() {
					protected Computer computer = CCAPIWrapper.this.computer;

					public Varargs invoke(Varargs args) {
						// Every time we call a method we should check the tryAbort function
						computer.tryAbort();
						return CCAPIWrapper.this.invoke(args, index);
					}
				};
				f.setfenv(env);

				// Allow multiple
				for (String name : methodNames[i]) {
					table.set(name, f);
				}
			}
		} catch (Exception e) {
			throw new LuaError("Bind failed: " + e);
		}
		return table;
	}
}
