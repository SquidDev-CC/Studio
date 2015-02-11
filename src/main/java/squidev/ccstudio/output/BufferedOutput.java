package squidev.ccstudio.output;

import java.util.Arrays;

/**
 * Buffers output
 */
public class BufferedOutput implements IOutput {
	public static final byte BLACK = 15;
	public static final byte WHITE = 0;

	protected static final TerminalConfig DEFAULT_CONFIG = new TerminalConfig(WIDTH, HEIGHT, true);

	protected byte[][] background;
	protected byte[][] foreground;
	protected byte[][] text;

	protected int cursorX = 0;
	protected int cursorY = 0;

	protected byte currentBackground = BLACK;
	protected byte currentForeground = WHITE;
	boolean cursorBlink = false;

	protected int width = WIDTH;
	protected int height = HEIGHT;

	/**
	 * Write a string to the terminal
	 *
	 * @param msg The string to write
	 */
	@Override
	public void write(byte[] msg) {
		int x = cursorX;
		int y = cursorY;
		int l = msg.length;
		System.arraycopy(msg, 0, text[y], x, l);

		// Fill in the values
		int last = x + l + 1;
		Arrays.fill(background[y], x, last, currentBackground);
		Arrays.fill(foreground[y], x, last, currentForeground);
	}

	/**
	 * Set cursor position
	 *
	 * @param x X position
	 * @param y Y position
	 */
	@Override
	public void setCursor(int x, int y) {
		cursorX = x;
		cursorY = y;
	}

	/**
	 * Set the text colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	@Override
	public void setTextColor(int col) {
		currentForeground = (byte) col;
	}

	/**
	 * Set the background colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	@Override
	public void setBackColor(int col) {
		currentBackground = (byte) col;
	}

	/**
	 * Set if the cursor should blink
	 *
	 * @param blink Set to true to make the cursor blink
	 */
	@Override
	public void setBlink(boolean blink) {
		this.cursorBlink = blink;
	}

	/**
	 * Scroll the output
	 *
	 * @param amount Positive number to scroll downwards, negative to scroll up
	 */
	@Override
	public void scroll(int amount) {
		int height = this.height;
		byte[][] text = this.text;
		byte[][] background = this.background;
		byte[][] foreground = this.foreground;

		int width = this.width;
		byte currentBackground = this.currentBackground;

		// Scroll up
		if (amount > 0) {
			int last = height - amount;
			int y;
			for (y = 0; y < last; y++) {
				text[y] = text[y + amount];
				background[y] = background[y + amount];
				foreground[y] = foreground[y + amount];
			}

			for (; y < height; y++) {
				byte[] newRow = new byte[width];
				Arrays.fill(newRow, (byte) ' ');
				text[y] = newRow;

				newRow = new byte[width];
				Arrays.fill(newRow, currentBackground);
				background[y] = newRow;

				newRow = new byte[width];
				Arrays.fill(newRow, WHITE);
				foreground[y] = newRow;
			}
		} else if (amount < 0) {
			int y;
			for (y = amount; y < height; y++) {
				text[y] = text[y + amount];
				background[y] = background[y - amount];
				foreground[y] = foreground[y - amount];
			}

			for (y = 0; y < amount; y++) {
				byte[] newRow = new byte[width];
				Arrays.fill(newRow, (byte) ' ');
				text[y] = newRow;

				newRow = new byte[width];
				Arrays.fill(newRow, currentBackground);
				background[y] = newRow;

				newRow = new byte[width];
				Arrays.fill(newRow, WHITE);
				foreground[y] = newRow;
			}
		}
	}

	/**
	 * Clear the console
	 */
	@Override
	public void clear() {
		int height = this.height;
		byte[][] background = this.background;
		byte[][] text = this.text;

		byte currentBack = this.currentBackground;
		for (int y = 0; y < height; y++) {
			Arrays.fill(background[y], currentBack);
			Arrays.fill(text[y], (byte) ' ');
		}
	}

	/**
	 * Clear the current line
	 */
	@Override
	public void clearLine() {
		int y = cursorY;
		byte currentBack = this.currentBackground;
		Arrays.fill(background[y], currentBack);
		Arrays.fill(text[y], (byte) ' ');
	}

	/**
	 * Gets the current config. May not be obeyed.
	 */
	@Override
	public ITerminalConfig getDefaults() {
		return DEFAULT_CONFIG;
	}

	/**
	 * Sets the current config
	 *
	 * @param config The config data
	 */
	@Override
	public void setConfig(ITerminalConfig config) {
		if (config.getWidth() != width || config.getHeight() != height) {
			int w = width = config.getWidth();
			int h = height = config.getHeight();

			byte[][] back = background = new byte[h][w];
			byte[][] fore = foreground = new byte[h][w];
			byte[][] text = this.text = new byte[h][w];
			for (int i = 0; i < h; i++) {
				Arrays.fill(back[i], currentBackground);
				Arrays.fill(text[i], (byte) ' ');
				Arrays.fill(text[i], currentForeground);
			}
		}
	}
}
