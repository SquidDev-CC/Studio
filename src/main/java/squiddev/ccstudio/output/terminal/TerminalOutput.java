package squiddev.ccstudio.output.terminal;

import jline.Terminal;
import jline.TerminalFactory;
import squiddev.ccstudio.output.IOutput;

import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * A config handler that outputs to the console
 */
public class TerminalOutput implements IOutput, IOutput.ITerminalConfig {
	public static final String START_ESCAPE = "\27[";

	public static final String HIDE_CURSOR = START_ESCAPE + "25l";
	public static final String SHOW_CURSOR = START_ESCAPE + "25h";

	public final PrintStream output;

	public static final Terminal TERMINAL;
	public static final boolean HAS_ANSI;
	public static final boolean SUPPORTED;

	public TerminalOutput() {
		output = System.out;
	}

	int oldY = 1;

	/**
	 * Maps a CC color to a ANSI color
	 * <p>
	 * This is much faster than using a hash map
	 *
	 * @param col The CC color to convert
	 * @return The ANSI color
	 */
	public static int getColor(int col) {
		switch (col) {
			case 0:
				return 97; // White
			case 1:
				return 33; // Orange
			case 2:
				return 95; // Magenta
			case 3:
				return 94; // Light blue
			case 4:
				return 93; // Yellow
			case 5:
				return 92; // Lime
			case 6:
				return 95; // No pink - use magenta (its not like anyone can tell the difference)
			case 7:
				return 90; // Gray (grey)
			case 8:
				return 37; // Light gray (grey)
			case 9:
				return 96; // Cyan
			case 10:
				return 35; // Purple (Dark magenta)
			case 11:
				return 36; // Blue
			case 12:
				return 31; // Brown
			case 13:
				return 32; // Green
			case 14:
				return 91; // Red
			case 15:
				return 30; // Black

			default:
				return 30;
		}
	}

	/**
	 * Write a string to the terminal
	 *
	 * @param msg The string to write
	 */
	@Override
	public void write(byte[] msg) {
		for (byte b : msg) output.print((char) b);
	}

	/**
	 * Set cursor position
	 *
	 * @param x X position
	 * @param y Y position
	 */
	@Override
	public void setCursor(int x, int y) {
		if (HAS_ANSI) {
			output.print(START_ESCAPE + x + ";" + y + "H");
		} else if (oldY != y) {
			output.println();
			oldY = y;
		}
	}

	/**
	 * Set the text colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	@Override
	public void setTextColor(int col) {
		if (HAS_ANSI) output.print(START_ESCAPE + getColor(col) + "m");
	}

	/**
	 * Set the background colour
	 *
	 * @param col The color (between 0 and 15 inclusive)
	 */
	@Override
	public void setBackColor(int col) {
		if (HAS_ANSI) output.print(START_ESCAPE + (getColor(col) + 10) + "m");
	}

	/**
	 * Set if the cursor should blink
	 *
	 * @param blink Set to true to make the cursor blink
	 */
	@Override
	public void setBlink(boolean blink) {
		if (!HAS_ANSI) return;
		if (blink) {
			output.print(SHOW_CURSOR);
		} else {
			output.print(HIDE_CURSOR);
		}
	}

	/**
	 * Scroll the output
	 *
	 * @param amount Positive number to scroll downwards, negative to scroll up
	 */
	@Override
	public void scroll(int amount) {
		if (!HAS_ANSI) return;
		if (amount > 0) {
			output.print(ansi().scrollDown(amount));
		} else if (amount < 0) {
			output.print(ansi().scrollUp(amount));
		}
	}

	/**
	 * Clear the console
	 */
	@Override
	public void clear() {
		if (!HAS_ANSI) return;
		output.print(ansi().eraseScreen());
	}

	/**
	 * Clear the current line
	 */
	@Override
	public void clearLine() {
		if (!HAS_ANSI) return;
		output.print(ansi().eraseLine());
	}

	/**
	 * Gets the current config. May not be obeyed.
	 */
	@Override
	public ITerminalConfig getDefaults() {
		return this;
	}

	/**
	 * Sets the current config.
	 *
	 * @param config The config data
	 */
	@Override
	public void setConfig(ITerminalConfig config) {
	}

	static {
		if (System.console() == null) {
			TERMINAL = null;
			HAS_ANSI = false;
			SUPPORTED = false;
		} else {
			TERMINAL = TerminalFactory.get();
			HAS_ANSI = TERMINAL.isAnsiSupported();
			SUPPORTED = true;
		}
	}

	@Override
	public boolean isColor() {
		return HAS_ANSI;
	}

	@Override
	public int getWidth() {
		return SUPPORTED ? TERMINAL.getWidth() : IOutput.WIDTH;
	}

	@Override
	public int getHeight() {
		return SUPPORTED ? TERMINAL.getWidth() : IOutput.HEIGHT;
	}
}
