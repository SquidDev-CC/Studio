package squidev.ccstudio.core.apis;

import org.luaj.vm2.LuaTable;

/**
 * An object that will be used as an API
 */
public interface ICCObject {
	public abstract LuaTable getTable();
}
