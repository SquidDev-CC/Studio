package org.squiddev.ccstudio.output.terminal;

import jline.console.ConsoleReader;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.squiddev.ccstudio.computer.Computer;
import org.squiddev.ccstudio.core.Config;
import org.squiddev.ccstudio.output.Keys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Create a computer and a terminal runner
 */
public class TerminalOutputMain {
	public static void main(String[] args) {
		Config config = new Config();
		Computer computer = new Computer(config, new TerminalOutput());

		computer.start();

		try {
			if (TerminalOutput.SUPPORTED) {
				ConsoleReader reader = new ConsoleReader();
				Keys k = new Keys();
				while (computer.isAlive()) {
					try {
						int character = reader.readCharacter();
						if (character < 0) break;
						Integer key = k.keys.get(character);
						if (key != null) {
							computer.queueEvent("key", LuaValue.valueOf(key));
						}
						if (character >= 32 && character <= 126) {
							computer.queueEvent("char", LuaValue.valueOf(new byte[]{(byte) character}));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				LuaInteger newLine = LuaValue.valueOf(28);

				while (computer.isAlive()) {
					String line = reader.readLine();
					if (line == null) {
						computer.shutdown(true);
						continue;
					}
					computer.queueEvent("paste", LuaValue.valueOf(line));
					computer.queueEvent("key", newLine);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
