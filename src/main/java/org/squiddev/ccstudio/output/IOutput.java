package org.squiddev.ccstudio.output;

/**
 * Basic functions to write to a terminal
 */
public interface IOutput {
	int WIDTH = 51;
	int HEIGHT = 19;

	/**
	 * Index of first character: 32 (" ")
	 */
	byte FIRST_CHAR = 32;

	ITerminalConfig DEFAULT_CONFIG = new TerminalConfig(WIDTH, HEIGHT, true);

	/**
	 * Write a string to the terminal
	 *
	 * @param msg The characters to write
	 */
	void write(byte[] msg);

	/**
	 * Set cursor position
	 *
	 * @param x X position
	 * @param y Y position
	 */
	void setCursor(int x, int y);

	/**
	 * Set the text colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	void setTextColor(int col);

	/**
	 * Set the background colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	void setBackColor(int col);

	/**
	 * Set if the cursor should blink
	 *
	 * @param blink Set to true to make the cursor blink
	 */
	void setBlink(boolean blink);

	/**
	 * Scroll the output
	 *
	 * @param amount Positive number to scroll downwards, negative to scroll up
	 */
	void scroll(int amount);

	/**
	 * Clear the console
	 */
	void clear();

	/**
	 * Clear the current line
	 */
	void clearLine();

	/**
	 * Gets the current config. May not be obeyed.
	 */
	ITerminalConfig getDefaults();

	/**
	 * Sets the current config
	 *
	 * @param config The config data
	 */
	void setConfig(ITerminalConfig config);

	interface ITerminalConfig {
		boolean isColor();

		int getWidth();

		int getHeight();
	}

	class TerminalConfig implements ITerminalConfig {
		protected final boolean color;
		protected final int width;
		protected final int height;

		public TerminalConfig(int width, int height, boolean color) {
			this.width = width;
			this.height = height;
			this.color = color;
		}

		@Override
		public boolean isColor() {
			return color;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
	}
}
