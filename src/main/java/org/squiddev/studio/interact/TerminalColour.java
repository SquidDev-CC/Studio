package org.squiddev.studio.interact;

public enum TerminalColour {
	BLACK(1644825),
	RED(13388876),
	GREEN(5744206),
	BROWN(8349260),
	BLUE(3368652),
	PURPLE(11691749),
	CYAN(5020082),
	LIGHT_GREY(10066329),
	GREY(5000268),
	PINK(15905484),
	LIME(8375321),
	YELLOW(14605932),
	LIGHT_BLUE(10072818),
	MAGENTA(15040472),
	ORANGE(15905331),
	WHITE(15790320);

	private int hex;
	private int[] rgb;

	TerminalColour(int hex) {
		this.hex = hex;
		this.rgb = new int[]{hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF};
	}

	public int getHex() {
		return this.hex;
	}

	public int[] getRGB() {
		return this.rgb;
	}

	public int getR() {
		return this.rgb[0];
	}

	public int getG() {
		return this.rgb[1];
	}

	public int getB() {
		return this.rgb[2];
	}

	private static TerminalColour[] values = values();

	public static TerminalColour fromCharacter(char character) {
		return values[indexFromCharacter(character)];
	}

	public static int indexFromCharacter(char character) {
		if (character >= '0' && character <= '9') {
			return 15 - (character - '0');
		} else {
			return 15 - (character - 'a' + 10);
		}
	}

}
