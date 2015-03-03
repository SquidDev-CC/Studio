package squiddev.ccstudio.output.lwjgl;

import squiddev.ccstudio.output.IOutput;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

/**
 * Handles font drawing
 */
public class Font {
	/**
	 * Characters per line on the font text
	 */
	protected static final int CHARACTERS_PER_LINE = 16;

	/**
	 * Lines to skip on the font before continuing
	 */
	protected static final int LINE_Y_OFFSET = 2;

	/**
	 * The texture for the font
	 */
	public final Texture texture;

	/**
	 * Height of each character in the font
	 */
	protected final int charHeight;

	/**
	 * Width of each character in the font
	 */
	protected final int charWidth;

	/**
	 * Height of one color block
	 */
	protected final int colourHeight;

	public Font() {
		TextureLoader textureLoader = new TextureLoader("squiddev/ccstudio/output/lwjgl");
		Texture texture;
		try {
			texture = textureLoader.getTexture("font.png");
		} catch (IOException e) {
			throw new RuntimeException("Cannot load font.png", e);
		}

		this.texture = texture;

		/*
			The character is about 16 wide. We get the actual character width & height to make things easier.
			Each colour block is 16 * 16 letters and there are 16 colour blocks.
		*/
		colourHeight = texture.getImageHeight() / 16;
		charHeight = colourHeight / 16;
		charWidth = texture.getImageWidth() / CHARACTERS_PER_LINE;
	}

	/**
	 * Draw a character
	 *
	 * @param character The character to draw
	 * @param color     The color to use
	 */
	public void drawCharacter(byte character, int color) {
		if (character == ' ' || character == 0) return;

		// Get character offsets
		int xOffset = character % CHARACTERS_PER_LINE;
		int yOffset = (character - IOutput.FIRST_CHAR) / CHARACTERS_PER_LINE + LINE_Y_OFFSET;

		xOffset *= charWidth;
		yOffset *= charHeight;
		yOffset += (15 - color) * colourHeight;
		System.out.println("Drawing " + (char) character + " at " + xOffset + ", " + yOffset);

		texture.bind();
		glBegin(GL_QUADS);
		{
			glTexCoord2f(xOffset, yOffset);
			glVertex2f(0, 0);

			glTexCoord2f(xOffset, yOffset + charHeight);
			glVertex2f(0, GuiOutput.CELL_HEIGHT);

			glTexCoord2f(xOffset + charHeight, yOffset + charHeight);
			glVertex2f(GuiOutput.CELL_WIDTH, GuiOutput.CELL_HEIGHT);

			glTexCoord2f(xOffset + charHeight, 0);
			glVertex2f(GuiOutput.CELL_WIDTH, 0);
		}
		glEnd();
	}
}
