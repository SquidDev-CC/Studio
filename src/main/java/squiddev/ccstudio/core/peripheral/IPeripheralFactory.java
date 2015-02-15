package squiddev.ccstudio.core.peripheral;

import squiddev.ccstudio.computer.Computer;

/**
 * A Factory class to generate peripherals
 */
public interface IPeripheralFactory {
	IPeripheral create(Computer computer, String side);
}
