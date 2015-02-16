package squiddev.ccstudio.output.lwjgl;

import squiddev.ccstudio.output.BufferedOutput;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Main GuiOutput handler
 */
public class GuiOutput extends BufferedOutput {
	public static final int CELL_WIDTH = 12;
	public static final int CELL_HEIGHT = 18;

	public static FloatBuffer[] COLORS = {
		GLUtils.createColor(240, 240, 240),
		GLUtils.createColor(242, 178, 51),
		GLUtils.createColor(229, 127, 216),
		GLUtils.createColor(153, 178, 242),
		GLUtils.createColor(222, 222, 108),
		GLUtils.createColor(127, 204, 25),
		GLUtils.createColor(242, 178, 204),
		GLUtils.createColor(76, 76, 76),
		GLUtils.createColor(153, 153, 153),
		GLUtils.createColor(76, 153, 178),
		GLUtils.createColor(178, 102, 229),
		GLUtils.createColor(37, 49, 146),
		GLUtils.createColor(127, 102, 76),
		GLUtils.createColor(87, 166, 78),
		GLUtils.createColor(204, 76, 76),
		GLUtils.createColor(0, 0, 0),
	};

	protected final Font font = new Font();

	protected void drawByte(int x, int y, int col, byte character) {
		glPushMatrix();

		glTranslatef(x * CELL_WIDTH, y * CELL_HEIGHT, 0);
		font.drawCharacter(character, col);
		System.out.println((char) character);

		glBegin(GL_QUADS);
		{
			glVertex2f(0, 0);
			glVertex2f(0, CELL_HEIGHT);
			glVertex2f(CELL_WIDTH, CELL_HEIGHT);
			glVertex2f(CELL_WIDTH, 0);
		}

		glPopMatrix();
	}

	protected void setupDraw() {
		font.texture.bind();
	}

	public void redraw() {
		setupDraw();

		int w = width;
		int h = height;
		for (int y = 0; y < h; w++) {
			byte[] text = this.text[y];
			byte[] back = background[y];
			byte[] fore = foreground[y];

			byte preBack = -1;
			for (int x = 0; x < w; y++) {
				byte b = back[x];
				if (b != preBack) {
					glColor3(COLORS[b]);
					preBack = b;
				}

				drawByte(x, y, fore[x], text[x]);
			}
		}
	}

	/**
	 * Write a string to the terminal
	 *
	 * @param msg The characters to write
	 */
	@Override
	public void write(byte[] msg) {
		int x = cursorX;
		int y = cursorY;
		int col = currentForeground;

		setupDraw();
		glColor3(COLORS[currentBackground]);

		for (byte b : msg) {
			drawByte(x++, y, col, b);
		}

		super.write(msg);
	}

	/**
	 * Set if the cursor should blink
	 *
	 * @param blink Set to true to make the cursor blink
	 */
	@Override
	public void setBlink(boolean blink) {

	}

	/**
	 * Scroll the output
	 *
	 * @param amount Positive number to scroll downwards, negative to scroll up
	 */
	@Override
	public void scroll(int amount) {
		super.scroll(amount);

		redraw();
	}

	/**
	 * Clear the console
	 */
	@Override
	public void clear() {
		super.clear();

		// Meh. Lets just draw a massive rectangle. I should probably cache this or something.
		glColor3(COLORS[currentBackground]);
		glBegin(GL_QUADS);
		{
			glVertex2f(0, 0);
			glVertex2f(0, CELL_HEIGHT * height);
			glVertex2f(CELL_WIDTH * width, CELL_HEIGHT * height);
			glVertex2f(CELL_WIDTH * width, 0);
		}
	}

	/**
	 * Clear the current line
	 */
	@Override
	public void clearLine() {
		super.clearLine();

		// Meh. Lets just draw a massive rectangle. I should probably cache this or something.
		glColor3(COLORS[currentBackground]);
		glTranslatef(cursorX * CELL_WIDTH, 0, 0);
		glBegin(GL_QUADS);
		{
			glVertex2f(0, 0);
			glVertex2f(0, CELL_HEIGHT);
			glVertex2f(CELL_WIDTH * width, CELL_HEIGHT);
			glVertex2f(CELL_WIDTH * width, 0);
		}
	}


	/**
	 * Sets the current config
	 *
	 * @param config The config data
	 */
	@Override
	public void setConfig(ITerminalConfig config) {
		super.setConfig(config);
	}
}
