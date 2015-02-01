package squidev.ccstudio.output.terminal;

import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.Config;

/**
 * Create a computer and a terminal runner
 */
public class TerminalOutputMain {
	public static void main(String[] args) {
		Config config = new Config();
		Computer computer = new Computer(config);

		computer.start();
	}
}
