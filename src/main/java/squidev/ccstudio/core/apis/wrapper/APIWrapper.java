package squidev.ccstudio.core.apis.wrapper;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import squidev.ccstudio.core.apis.CCAPI;

/**
 * Basic wrapper for APIs built with {@see APIBuilder}
 */
public abstract class APIWrapper<T> extends CCAPI {
	public final T instance;

	protected String[][] methodNames = null;
	protected String[] names = null;

	protected LuaTable table;

	public APIWrapper(T inst) {
		instance = inst;
	}

	/**
	 * Returns the variables this API should be stored in
	 * @return List of variable names
	 */
	public String[] getNames() {
		return names;
	}

	public abstract Varargs invoke(Varargs args, int index);

	/**
	 * Convert this to a Lua table
	 * @return The API object
	 */
	public LuaTable getTable() {
		if(table == null) {
			table = new LuaTable();
			try {
				for (int i = 0, n = methodNames.length; i < n; i++) {
					final int index = i;
					LuaFunction f = new VarArgFunction() {
						public Varargs invoke(Varargs args) {
							return APIWrapper.this.invoke(args, index);
						}
					};

					// Allow multiple
					for (String name : methodNames[i]) {
						table.set(name, f);
					}
				}
			} catch ( Exception e ) {
				throw new LuaError("Bind failed: " + e);
			}
		}
		return table;
	}
}
