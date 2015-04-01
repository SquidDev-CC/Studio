package org.squiddev.ccstudio.core.peripheral;

import org.squiddev.ccstudio.computer.Computer;

/**
 * A Factory class to generate peripherals
 */
public interface IPeripheralFactory {
	IPeripheral create(Computer computer, String side);
}
