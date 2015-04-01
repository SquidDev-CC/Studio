package org.squiddev.ccstudio.output;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping of keys to CC's key system.
 */
public class Keys {
	public final Map<Integer, Integer> keys = new HashMap<>();
	public int latest = 0;

	public void put(int key, int code) {
		if (keys.get(key) != null) System.err.println("Key " + key + " has " + keys.get(key) + " setting to " + code);
		keys.put(key, code);
		latest = Math.max(latest, code);
	}

	public void put(char key, int code) {
		put((int) Character.toUpperCase(key), code);
	}

	public void put(int key) {
		put(key, latest + 1);
	}

	public void put(char key) {
		put(key, latest + 1);
	}

	public Keys() {
		put(27);
		put('1');
		put('2');
		put('3');
		put('4');
		put('5');
		put('6');
		put('7');
		put('8');
		put('9');
		put('0');
		put('-');
		put('=');
		put(259); // backspace
		put('\t');
		put('q');
		put('w');
		put('e');
		put('r');
		put('t');
		put('y');
		put('u');
		put('i');
		put('o');
		put('p');
		put('[');
		put(']');
		put('\n');
		put(257, latest);
		put(341); // Left Ctrl
		put('a');
		put('s');
		put('d');
		put('f');
		put('g');
		put('h');
		put('j');
		put('k');
		put('l');
		put(';');
		put('\'');
		put('~');
		put(340); // Left shift
		put('\\');
		put('z');
		put('x');
		put('c');
		put('v');
		put('b');
		put('n');
		put('m');
		put(',');
		put('.');
		put('/');
		put(344); // Right Shift
		put('*');
		put(342); // Left Alt
		put(' ');
		put(280); // Caps lock
		for (int i = 290; i < 300; i++) {
			put(i); // F1-10
		}
		put(282); // Num-lock
		put(281); // Scroll lock

		put(327); // Num-7
		put(328); // Num-8
		put(329); // Num-9
		put(333); // Num-Subtract
		put(324); // Num-4
		put(325); // Num-5
		put(326); // Num-6
		put(334); // Num-Add
		put(321); // Num-1
		put(322); // Num-2
		put(323); // Num-3
		put(320); // Num-0
		put(330); // Num-Decimal

		put(345, 153); // Right control
		put(331, 181); // Num-Divide
		put(346, 184); // Right thing

		put(268, 199); // Home
		put(265); // Up
		put(266); // Page Up

		put(263, 203); // Left
		put(262, 205); // Right

		put(269, 207); // End
		put(264);
		put(267); // Page down
		put(260); // Insert
		put(261); // Delete
	}
}
