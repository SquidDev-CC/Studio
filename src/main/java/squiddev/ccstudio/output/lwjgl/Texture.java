/*
 * Copyright (c) 2002-2010 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package squiddev.ccstudio.output.lwjgl;

import static org.lwjgl.opengl.GL11.*;

/**
 * A texture to be bound within OpenGL. This object is responsible for
 * keeping track of a given OpenGL texture and for calculating the
 * texturing mapping coordinates of the full image.
 * <p>
 * Since textures need to be powers of 2 the actual texture may be
 * considerably bigged that the source image and hence the texture
 * mapping coordinates need to be adjusted to matchup drawing the
 * sprite against the texture.
 */
public class Texture {

	/**
	 * The GL target type
	 */
	private final int target;

	/**
	 * The GL texture ID
	 */
	private final int textureID;

	/**
	 * The height of the image
	 */
	private final int height;

	/**
	 * The width of the image
	 */
	private final int width;

	/**
	 * The ratio of the width of the image to the texture
	 */
	private final float widthRatio;

	/**
	 * The ratio of the height of the image to the texture
	 */
	private final float heightRatio;

	/**
	 * Create a new texture
	 *
	 * @param target    The GL target
	 * @param textureID The GL texture ID
	 */
	public Texture(int target, int textureID, int width, int height) {
		this.target = target;
		this.textureID = textureID;

		this.width = width;
		this.height = height;

		this.widthRatio = 1.0f / width;
		this.heightRatio = 1.0f / height;
	}

	/**
	 * Bind the specified GL context to a texture
	 */
	public void bind() {
		glBindTexture(target, textureID);
	}

	public void bindCoords(int x, int y) {
		glTexCoord2f(x * widthRatio, y * heightRatio);
	}

	/**
	 * Get the height of the original image
	 *
	 * @return The height of the original image
	 */
	public int getImageHeight() {
		return height;
	}

	/**
	 * Get the width of the original image
	 *
	 * @return The width of the original image
	 */
	public int getImageWidth() {
		return width;
	}

	/**
	 * Get the height ratio
	 *
	 * @return The height of physical texture
	 */
	public float getHeightRatio() {
		return heightRatio;
	}

	/**
	 * Get the width ratio
	 *
	 * @return The width of physical texture
	 */
	public float getWidth() {
		return widthRatio;
	}

	public void render(float imageX, float imageY, float imageWidth, float imageHeight, float x, float y, float width, float height) {
		bind();

		float wRatio = widthRatio;
		float hRatio = heightRatio;

		imageWidth = (imageWidth + imageX) * wRatio;
		imageX *= wRatio;
		imageHeight = (imageHeight + imageY) * hRatio;
		imageY *= hRatio;

		width += x;
		height += y;


		glBegin(GL_QUADS);
		{
			glTexCoord2f(imageX, imageY);
			glVertex2f(x, y);

			glTexCoord2f(imageX, imageHeight);
			glVertex2f(x, height);

			glTexCoord2f(imageWidth, imageHeight);
			glVertex2f(width, height);

			glTexCoord2f(imageWidth, imageY);
			glVertex2f(width, y);
		}
		glEnd();
	}
}
