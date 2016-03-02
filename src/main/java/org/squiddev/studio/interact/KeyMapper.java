package org.squiddev.studio.interact;

/**
 * Utility for mapping keys
 */
public final class KeyMapper {
	private KeyMapper() {
	}

	public static int KEY_RESTART = 19;
	public static int KEY_TERMINATE = 20;
	public static int KEY_SHUTDOWN = 31;

	private static final byte[] keys = new byte[256];

	private static void add(char character, int key) {
		keys[character & 0xFF] = (byte) key;
	}

	public static int get(char character) {
		int result = keys[character & 0xFF];
		return result <= 0 ? -1 : result;
	}

	static {
		add('1', 2);
		add('2', 3);
		add('3', 4);
		add('4', 5);
		add('5', 6);
		add('6', 7);
		add('7', 8);
		add('8', 9);
		add('9', 10);
		add('0', 11);
		add('-', 12);
		add('=', 13);
		// add('backspace', 14);
		add('\t', 15);
		add('q', 16);
		add('w', 17);
		add('e', 18);
		add('r', 19);
		add('t', 20);
		add('y', 21);
		add('u', 22);
		add('i', 23);
		add('o', 24);
		add('p', 25);
		add('(', 26);
		add(')', 27);
		add('\n', 28);
		// add('leftCtrl', 29);
		add('a', 30);
		add('s', 31);
		add('d', 32);
		add('f', 33);
		add('g', 34);
		add('h', 35);
		add('j', 36);
		add('k', 37);
		add('l', 38);
		add(';', 39);
		add('-', 40);
		add('~', 41);
		// add('leftShift', 42);
		add('\\', 43);
		add('z', 44);
		add('x', 45);
		add('c', 46);
		add('v', 47);
		add('b', 48);
		add('n', 49);
		add('m', 50);
		add(',', 51);
		add('.', 52);
		add('/', 53);
		// add('rightShift', 54);
		add('*', 55);
		// add('leftAlt', 56);
		add(' ', 57);
		// add('capsLock', 58);
		// add('f1', 59);
		// add('f2', 60);
		// add('f3', 61);
		// add('f4', 62);
		// add('f5', 63);
		// add('f6', 64);
		// add('f7', 65);
		// add('f8', 66);
		// add('f9', 67);
		// add('f10', 68);
		// add('numLock', 69);
		// add('scollLock', 70);
		// add('numPad7', 71);
		// add('numPad8', 72);
		// add('numPad9', 73);
		// add('numPadSubtract', 74);
		// add('numPad4', 75);
		// add('numPad5', 76);
		// add('numPad6', 77);
		// add('numPadAdd', 78);
		// add('numPad1', 79);
		// add('numPad2', 80);
		// add('numPad3', 81);
		// add('numPad0', 82);
		// add('numPadDecimal', 83);
		// add('f11', 87);
		// add('f12', 88);
		// add('f13', 100);
		// add('f14', 101);
		// add('f15', 102);
		// add('kana', 112);
		// add('convert', 121);
		// add('noconvert', 123);
		// add('yen', 125);
		// add('numPadEquals', 141);
		add('^', 144);
		add('@', 145);
		add(':', 146);
		add('_', 147);
		// add('kanji', 148);
		// add('stop', 149);
		// add('ax', 150);
		// add('numPadEnter', 156);
		// add('rightCtrl', 157);
		// add('numPadComma', 179);
		// add('numPadDivide', 181);
		// add('rightAlt', 184);
		// add('pause', 197);
		// add('home', 199);
		// add('up', 200);
		// add('pageUp', 201);
		// add('left', 203);
		// add('right', 205);
		// add('end', 207);
		// add('down', 208);
		// add('pageDown', 209);
		// add('insert', 210);
		// add('delete', 211);
	}
}
