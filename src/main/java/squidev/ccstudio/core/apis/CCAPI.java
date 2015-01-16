package squidev.ccstudio.core.apis;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Core computercraft API
 */
public abstract class CCAPI implements ICCObject {
	/**
	 * Get the names this API should be stored in
	 */
	public abstract String[] getNames();

	public LuaTable getTable() {
		return getTable(null);
	}

	public abstract LuaTable getTable(LuaValue env);

	/**
	 * Bind this API to an environment
	 */
	public void bind(LuaValue env) {
		String[] names = getNames();
		if (names != null) {
			LuaTable t = getTable(env);
			for (String name : names) {
				env.set(name, t);
			}
		}
	}
}
