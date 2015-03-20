package squiddev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;
import squiddev.ccstudio.core.apis.wrapper.StrictValidator;
import squiddev.ccstudio.core.apis.wrapper.ValidationClass;
import squiddev.ccstudio.output.IOutput;

/**
 * Implements the terminal API
 */
@LuaAPI("term")
@ValidationClass(StrictValidator.class)
public class TerminalAPI {
	public final int width;
	public final int height;

	public final Varargs size;

	public final IOutput output;
	public final boolean hasColor;
	public int cursorX = 0;
	public int cursorY = 0;

	public TerminalAPI(IOutput output, IOutput.ITerminalConfig config) {
		output.setConfig(config);

		this.width = config.getWidth();
		this.height = config.getHeight();

		this.size = LuaValue.varargsOf(LuaValue.valueOf(width), LuaValue.valueOf(height));

		this.hasColor = config.isColor();

		this.output = output;
	}

	public TerminalAPI(IOutput output) {
		this(output, output.getDefaults());
	}

	public static TerminalAPI defaults(IOutput output) {
		return new TerminalAPI(output, IOutput.DEFAULT_CONFIG);
	}

	@LuaFunction
	public void write(String text) {
		int length = text.length();
		if (cursorX < width && cursorY >= 0 && cursorY < height && length > 0) {
			byte[] bytes = text.substring(
				Math.max(0, -cursorX),
				Math.min(length, width - cursorX)
			).getBytes();

			int byteLength = bytes.length;
			for (int i = 0; i < byteLength; i++) {
				byte character = bytes[i];
				if (character == '\t') {
					bytes[i] = ' ';
				} else if (character < ' ' || character > '~') {
					bytes[i] = '?';
				}
			}

			output.write(bytes);
			setInternalCursorPos(cursorX + length, cursorY);
		}
	}

	@LuaFunction
	public void clear() {
		output.clear();
	}

	@LuaFunction
	public void clearLine() {
		output.clearLine();
	}

	@LuaFunction
	public Varargs getCursorPos() {
		return LuaValue.varargsOf(LuaValue.valueOf(cursorX), LuaValue.valueOf(cursorY));
	}

	@LuaFunction
	public void setCursorPos(double x, double y) {
		setInternalCursorPos((int) Math.floor(x - 1), (int) Math.floor(y - 1));
	}

	protected void setInternalCursorPos(int x, int y) {
		cursorX = x;
		cursorY = y;

		output.setCursor(x, y);
	}

	@LuaFunction
	public void setCursorBlink(boolean blink) {
		output.setBlink(blink);
	}

	@LuaFunction({"isColor", "isColour"})
	public boolean isColor() {
		return hasColor;
	}

	@LuaFunction
	public Varargs getSize() {
		return size;
	}

	@LuaFunction
	public void scroll(int amount) {
		output.scroll(amount);
	}

	@LuaFunction({"setTextColor", "setTextColour"})
	public void setTextColor(double color) {
		output.setTextColor(validateColor(color));
	}

	@LuaFunction({"setBackgroundColor", "setBackgroundColour"})
	public void setBackgroundColor(double color) {
		output.setBackColor(validateColor(color));
	}

	protected int validateColor(double color) {
		int result = (int) Math.floor(Math.log(color) / Math.log(2));

		if (result < 0 || result > 15) throw new LuaError("Colour out of range");
		if (!hasColor && result != 0 && result != 15) throw new LuaError("Colour not supported");

		return result;
	}
}
