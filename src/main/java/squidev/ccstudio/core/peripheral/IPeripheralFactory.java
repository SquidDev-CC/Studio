package squidev.ccstudio.core.peripheral;

import squidev.ccstudio.computer.Computer;

/**
 * A Factory class to generate peripherals
 */
public interface IPeripheralFactory {
	IPeripheral create(Computer computer, String side);
}
