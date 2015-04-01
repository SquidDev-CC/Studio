package org.squiddev.ccstudio.output.lwjgl;

import static org.lwjgl.opengl.GL11.glColor3ub;

/**
 * Utils for the nuisance that is OpenGL
 */
public class GLUtils {
	public static class Color {
		protected final byte r;
		protected final byte g;
		protected final byte b;

		public Color(byte r, byte g, byte b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public Color(int r, int g, int b) {
			this((byte) r, (byte) g, (byte) b);
		}

		public void use() {
			glColor3ub(r, g, b);
		}

		@Override
		public String toString() {
			return "Color{" + r + ", " + g + ", " + b + '}';
		}
	}
}
