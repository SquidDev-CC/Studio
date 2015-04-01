package org.squiddev.ccstudio.core.peripheral;

import org.squiddev.luaj.api.LuaObject;

/**
 * A peripheral for a computer
 */
public abstract class IPeripheral extends LuaObject {
	/**
	 * Get the type of peripheral
	 *
	 * @return Peripheral name
	 */
	public abstract String getType();
}
