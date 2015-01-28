package squidev.ccstudio.core.peripheral;

import squidev.ccstudio.core.apis.ICCObject;

/**
 * A peripheral for a computer
 */
public interface IPeripheral extends ICCObject {
	/**
	 * Get the type of peripheral
	 *
	 * @return Peripheral name
	 */
	String getType();
}
