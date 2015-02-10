package squidev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.core.apis.wrapper.LuaAPI;
import squidev.ccstudio.core.apis.wrapper.LuaFunction;
import squidev.ccstudio.output.IOutput;

/**
 * Implements the terminal API
 */
@LuaAPI("term")
public class TerminalAPI {
	public final int width;
	public final int height;

	public final Varargs size;

	public final IOutput output;
	public final boolean hasColor;
	public int cursorX = 0;
	public int cursorY = 0;

	public TerminalAPI(int width, int height, boolean color, IOutput output) {
		this.width = width;
		this.height = height;

		this.size = LuaValue.varargsOf(LuaValue.valueOf(width), LuaValue.valueOf(height));

		this.hasColor = color;

		this.output = output;
	}

	public TerminalAPI(IOutput output) {
		this(51, 19, true, output);
	}

	public static TerminalAPI calculateSize(IOutput output) {
		int[] size = output.getSize();
		return new TerminalAPI(size[0], size[1], true, output);
	}

	@LuaFunction
	public void write(String text) {
		int length = text.length();
		if (cursorX < width && cursorY >= 0 && cursorY < height && length > 0) {
			output.write(text.replace('\t', ' ').substring(
					Math.max(0, -cursorX),
					Math.min(length, width - cursorX)
			));
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
		output.setTextColor(validateColor(color));
	}

	protected int validateColor(double color) {
		int result = (int) Math.floor(Math.log(color) / Math.log(2));

		if (result < 0 || result > 15) throw new LuaError("Colour out of range");
		if (!hasColor && color != 0 && color != 15) throw new LuaError("Colour not supported");

		return result;
	}
}
