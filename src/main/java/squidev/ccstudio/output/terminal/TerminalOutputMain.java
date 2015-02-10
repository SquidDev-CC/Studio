package squidev.ccstudio.output.terminal;

import jline.console.ConsoleReader;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Create a computer and a terminal runner
 */
public class TerminalOutputMain {
	public static final Map<Integer, Integer> KEYS;

	public static void main(String[] args) {
		Config config = new Config();
		Computer computer = new Computer(config);

		computer.start();

		try {
			if (TerminalOutput.SUPPORTED) {
				ConsoleReader reader = new ConsoleReader();
				while (computer.isAlive()) {
					try {
						int character = reader.readCharacter();
						if (character < 0) break;
						Integer key = KEYS.get(character);
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

	static {
		KeyHelper keys = new KeyHelper();
		KEYS = keys.keys;

		keys.put(27, 1);
		keys.put('1');
		keys.put('2');
		keys.put('3');
		keys.put('4');
		keys.put('5');
		keys.put('6');
		keys.put('7');
		keys.put('8');
		keys.put('9');
		keys.put('0');
		keys.put('-');
		keys.put('=');
		keys.put(-1); // backspace
		keys.put('\t');
		keys.put('q');
		keys.put('w');
		keys.put('e');
		keys.put('r');
		keys.put('t');
		keys.put('y');
		keys.put('u');
		keys.put('i');
		keys.put('o');
		keys.put('p');
		keys.put('[');
		keys.put(']');
		keys.put('\n');
		keys.put(-1); // Left Ctrl
		keys.put('a');
		keys.put('s');
		keys.put('d');
		keys.put('f');
		keys.put('g');
		keys.put('h');
		keys.put('j');
		keys.put('k');
		keys.put('l');
		keys.put(';');
		keys.put('\'');
		keys.put('~');
		keys.put(-1); // Left shift
		keys.put('\\');
		keys.put('z');
		keys.put('x');
		keys.put('c');
		keys.put('v');
		keys.put('b');
		keys.put('n');
		keys.put('m');
		keys.put(',');
		keys.put('.');
		keys.put('/');
		keys.put(-1); // Right Shift
		keys.put('*');
		keys.put(-1); // Left Alt
		keys.put(' '); // Space
	}

	/**
	 * Simple methods to help key mapping be easier
	 */
	private static class KeyHelper {
		public final Map<Integer, Integer> keys = new HashMap<>();
		public int latest = 1;

		public void put(int key, int code) {
			keys.put(key, code);
			latest = Math.max(latest, code);
		}

		public void put(char key, int code) {
			keys.put((int) key, code);
			latest = Math.max(latest, code);
		}

		public void put(int key) {
			put(key, latest + 1);
		}

		public void put(char key) {
			put(key, latest + 1);
		}
	}
}
