package org.squiddev.studio.computer;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.terminal.Terminal;
import org.squiddev.studio.storage.ComputerInfo;
import org.squiddev.studio.storage.Session;
import org.squiddev.studio.storage.Side;

/**
 * Holder for a computer
 */
public class ComputerManager {
	public final Session session;
	public final ComputerInfo computerInfo;

	private final Computer computer;
	public final Terminal terminal;

	public ComputerManager(Session session, ComputerInfo computerInfo) {
		this.session = session;
		this.computerInfo = computerInfo;

		terminal = new Terminal(computerInfo.termWidth, computerInfo.termHeight);
		computer = new Computer(new ComputerEnvironment(session, computerInfo), terminal, computerInfo.id);
		syncToComputer();
		computer.turnOn();
	}

	public void syncToComputer() {
		computer.setLabel(computerInfo.label);
		computer.setID(computerInfo.id);

		for (int i = 0; i < computerInfo.sides.length; i++) {
			Side side = computerInfo.sides[i];

			computer.setRedstoneInput(i, side.redstoneInput);
			computer.setBundledRedstoneInput(i, side.bundledInput);
			computer.setPeripheral(i, PeripheralRegistry.loadPeripheral(side.peripheral));
		}
	}

	public void syncToInfo() {
		computerInfo.label = computer.getLabel();
		computerInfo.id = computer.getID();

		for (int i = 0; i < computerInfo.sides.length; i++) {
			Side side = computerInfo.sides[i];

			side.redstoneInput = computer.getRedstoneOutput(i);
			side.bundledInput = computer.getBundledRedstoneOutput(i);
			IPeripheral peripheral = computer.getPeripheral(i);
			if (peripheral == null) {
				side.peripheral = null;
			} else {
				side.peripheral = peripheral.getType();
			}
		}
	}

	public void unload() {
		computer.unload();
		syncToInfo();
	}

	public boolean update() {
		computer.advance(0.05d);
		if (computer.pollChanged()) {
			computer.clearChanged();
			syncToInfo();
			return true;
		}

		return false;
	}

	public boolean pollTerminal() {
		boolean changed = terminal.getChanged();
		terminal.clearChanged();
		return changed;
	}

	public boolean isOn() {
		return computer.isOn();
	}

	public boolean isBlinking() {
		return computer.isBlinking();
	}

	public void shutdown() {
		computer.shutdown();
	}

	public void restart() {
		computer.reboot();
	}

	public void queueEvent(String name, Object... args) {
		computer.queueEvent(name, args);
	}
}
