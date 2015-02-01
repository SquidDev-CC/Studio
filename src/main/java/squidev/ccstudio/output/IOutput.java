package squidev.ccstudio.output;

/**
 * Basic functions to write to a terminal
 */
public interface IOutput {
	/**
	 * Write a string to the terminal
	 *
	 * @param msg The string to write
	 */
	public abstract void write(String msg);

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
}
