package squidev.ccstudio.output;

/**
 * Basic functions to write to a terminal
 */
public interface IOutput {
	public static final int WIDTH = 51;
	public static final int HEIGHT = 19;

	/**
	 * Write a string to the terminal
	 *
	 * @param msg The characters to write
	 */
	public abstract void write(byte[] msg);

	/**
	 * Set cursor position
	 *
	 * @param x X position
	 * @param y Y position
	 */
	public abstract void setCursor(int x, int y);

	/**
	 * Set the text colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	public abstract void setTextColor(int col);

	/**
	 * Set the background colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	public abstract void setBackColor(int col);

	/**
	 * Set if the cursor should blink
	 *
	 * @param blink Set to true to make the cursor blink
	 */
	public abstract void setBlink(boolean blink);

	/**
	 * Scroll the output
	 *
	 * @param amount Positive number to scroll downwards, negative to scroll up
	 */
	public abstract void scroll(int amount);

	/**
	 * Clear the console
	 */
	public abstract void clear();

	/**
	 * Clear the current line
	 */
	public abstract void clearLine();

	/**
	 * Gets the current config. May not be obeyed.
	 */
	public abstract ITerminalConfig getDefaults();

	/**
	 * Sets the current config
	 *
	 * @param config The config data
	 */
	public abstract void setConfig(ITerminalConfig config);

	public static interface ITerminalConfig {
		public abstract boolean isColor();

		public abstract int getWidth();

		public abstract int getHeight();
	}

	public class TerminalConfig implements ITerminalConfig {
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
