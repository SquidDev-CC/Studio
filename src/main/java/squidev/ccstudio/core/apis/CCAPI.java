package squidev.ccstudio.core.apis;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * Basic wrapper for APIs build with {@see APIBuilder}
 */
public abstract class CCAPI<T> extends VarArgFunction {
	public final T instance;

	protected String[] methodNames = new String[0];
	protected String[] names = new String[0];

	protected LuaTable table;

	public CCAPI(T inst) {
		instance = inst;
	}

	/**
	 * Returns the variables this API should be stored in
	 * @return List of variable names
	 */
	public String[] getNames() {
		return names;
	}

	/**
	 * Convert this to a Lua table
	 * @return The API object
	 */
	public LuaTable getTable() {
		if(table == null) {
			table = new LuaTable();
			bind(table, this.getClass(), methodNames, 0);
		}
		return table;
	}

	/**
	 * Bind this API to an environment
	 */
	public void bind() {
		LuaTable t = getTable();
		for(String name : getNames()) {
			env.set(name, t);
		}
	}
}
