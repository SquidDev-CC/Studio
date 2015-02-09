package squidev.ccstudio.output.terminal;

import jline.console.ConsoleReader;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.Config;

import java.io.IOException;

/**
 * Create a computer and a terminal runner
 */
public class TerminalOutputMain {
	public static void main(String[] args) {
		Config config = new Config();
		Computer computer = new Computer(config);

		computer.start();

		try {
			ConsoleReader reader = new ConsoleReader();
			while (true) {
				try {
					int character = reader.readCharacter();
					if (character < 0) break;
					System.out.println(character);
				} catch (IOException e) {

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			TerminalOutput.terminal.restore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
