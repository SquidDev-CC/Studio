package squidev.ccstudio.core.peripheral;

import squidev.ccstudio.core.apis.ICCObject;

/**
 * A peripheral for a computer
 */
public interface IPeripheral {
	/**
	 * Get the object needed
	 *
	 * @return The CC object
	 */
	ICCObject getObject();

	/**
	 * Get the type of peripheral
	 *
	 * @return Peripheral name
	 */
	String getType();
}
