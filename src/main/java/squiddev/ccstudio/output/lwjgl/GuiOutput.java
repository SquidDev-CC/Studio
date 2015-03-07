package squiddev.ccstudio.output.lwjgl;

import squiddev.ccstudio.output.BufferedOutput;

import static org.lwjgl.opengl.GL11.*;

/**
 * Main GuiOutput handler
 */
public class GuiOutput extends BufferedOutput {
	public static final int CELL_WIDTH = 12;
	public static final int CELL_HEIGHT = 18;

	public static GLUtils.Color[] COLORS = {
			new GLUtils.Color(240, 240, 240),
			new GLUtils.Color(242, 178, 51),
			new GLUtils.Color(229, 127, 216),
			new GLUtils.Color(153, 178, 242),
			new GLUtils.Color(222, 222, 108),
			new GLUtils.Color(127, 204, 25),
			new GLUtils.Color(242, 178, 204),
			new GLUtils.Color(76, 76, 76),
			new GLUtils.Color(153, 153, 153),
			new GLUtils.Color(76, 153, 178),
			new GLUtils.Color(178, 102, 229),
			new GLUtils.Color(37, 49, 146),
			new GLUtils.Color(127, 102, 76),
			new GLUtils.Color(87, 166, 78),
			new GLUtils.Color(204, 76, 76),
			new GLUtils.Color(0, 0, 0),
	};

	protected final Font font = new Font();

	public boolean dynamicDraw = false;

	protected void drawByte(int x, int y, int foreground, byte character) {
		glPushMatrix();

		glTranslatef(x * CELL_WIDTH, y * CELL_HEIGHT, 0);

		glBegin(GL_QUADS);
		{
			glVertex2f(0, 0);
			glVertex2f(0, CELL_HEIGHT);
			glVertex2f(CELL_WIDTH, CELL_HEIGHT);
			glVertex2f(CELL_WIDTH, 0);
		}
		glEnd();

		font.drawCharacter(character, foreground);

		glPopMatrix();
	}

	public void redraw() {
		int w = width;
		int h = height;

		byte previousBackground = -1;
		for (int y = 0; y < h; y++) {
			byte[] text = this.text[y];
			byte[] back = background[y];
			byte[] fore = foreground[y];

			for (int x = 0; x < w; x++) {
				byte background = back[x];
				if (background != previousBackground) {
					COLORS[background].use();
					previousBackground = background;
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
		if (dynamicDraw) {
			int x = cursorX;
			int y = cursorY;
			int col = currentForeground;

			COLORS[currentBackground].use();

			for (byte b : msg) {
				drawByte(x++, y, col, b);
			}
		}

		super.write(msg);
	}

	/**
	 * Scroll the output
	 *
	 * @param amount Positive number to scroll downwards, negative to scroll up
	 */
	@Override
	public void scroll(int amount) {
		super.scroll(amount);

		if (dynamicDraw) redraw();
	}

	/**
	 * Clear the console
	 */
	@Override
	public void clear() {
		super.clear();

		if (dynamicDraw) {
			// Meh. Lets just draw a massive rectangle. I should probably cache this or something.
			COLORS[currentBackground].use();
			glBegin(GL_QUADS);
			{
				glVertex2f(0, 0);
				glVertex2f(0, CELL_HEIGHT * height);
				glVertex2f(CELL_WIDTH * width, CELL_HEIGHT * height);
				glVertex2f(CELL_WIDTH * width, 0);
			}
			glEnd();
		}
	}

	/**
	 * Clear the current line
	 */
	@Override
	public void clearLine() {
		super.clearLine();

		if (dynamicDraw) {
			// Meh. Lets just draw a massive rectangle. I should probably cache this or something.
			COLORS[currentBackground].use();
			glTranslatef(cursorX * CELL_WIDTH, 0, 0);
			glBegin(GL_QUADS);
			{
				glVertex2f(0, 0);
				glVertex2f(0, CELL_HEIGHT);
				glVertex2f(CELL_WIDTH * width, CELL_HEIGHT);
				glVertex2f(CELL_WIDTH * width, 0);
			}
			glEnd();
		}
	}
}
