package org.squiddev.ccstudio.output.lwjgl;

import org.squiddev.ccstudio.output.IOutput;

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

	/**
	 * Widths of each character
	 */
	protected final int[] characterWidths = {
		0, 2, 8, 10, 10, 10, 10, 4, 8, 8, 8, 10, 2, 10, 2, 10, 10, 10, 10,
		10, 10, 10, 10, 10, 10, 10, 2, 2, 8, 10, 8, 10, 12, 10, 10, 10, 10,
		10, 10, 10, 10, 6, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
		10, 10, 10, 10, 10, 6, 10, 6, 10, 10, 4, 10, 10, 10, 10, 10, 8, 10,
		10, 2, 10, 8, 4, 10, 10, 10, 10, 10, 10, 10, 6, 10, 10, 10, 10, 10,
		10, 8, 2, 8, 12, 0
	};

	public Font() {

		Texture texture = this.texture = TextureLoader.loadTexture("/org/squiddev/ccstudio/output/lwjgl/font.png");

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
		if (character <= ' ') return;

		// Get character offsets
		int charLoc = character - IOutput.FIRST_CHAR;
		int xTexture = character % CHARACTERS_PER_LINE;
		int yTexture = charLoc / CHARACTERS_PER_LINE + LINE_Y_OFFSET;

		xTexture *= charWidth;
		yTexture *= charHeight;
		yTexture += (15 - color) * colourHeight; // We need to invert the color

//		int offset = charWidth / 2 - characterWidths[charLoc] / 2 - 1;
//		if (character == '@' || character  == '~') --offset;

		texture.render(xTexture, yTexture, charWidth, charHeight, 0, 0, GuiOutput.CELL_WIDTH, GuiOutput.CELL_HEIGHT);
	}
}
