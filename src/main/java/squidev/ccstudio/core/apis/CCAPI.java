package squidev.ccstudio.core.apis;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Core computercraft API
 */
public abstract class CCAPI implements ICCObject {
	protected LuaValue env = LuaValue.NIL;
	protected LuaTable table;
	/**
	 * Get the names this API should be stored in
	 */
	public abstract String[] getNames();

	protected abstract LuaTable createTable();

	public LuaTable getTable() {
		if (table == null) {
			table = createTable();
		}
		return table;
	}

	/**
	 * Bind this API to an environment
	 */
	public void bind() {
		String[] names = getNames();
		if (names != null) {
			LuaTable t = getTable();
			for (String name : names) {
				env.set(name, t);
			}
		}
	}

	/**
	 * Get the environment to execute under
	 *
	 * @return The current environment
	 */
	public LuaValue getEnv() {
		return env;
	}

	/**
	 * Set the executor environment
	 *
	 * @param env The environment to use
	 */
	public void setEnv(LuaValue env) {
		if (env != this.env) {
			// Clear the table if not the same
			this.env = env;
			this.table = null;
		}
	}
}
