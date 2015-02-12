package squidev.ccstudio.output.lwjgl;

import java.nio.FloatBuffer;

/**
 * Utils for the nuisance that is OpenGL
 */
public class GLUtils {
	public static FloatBuffer createColor(int r, int g, int b) {
		return FloatBuffer.wrap(new float[]{
			(float) r / 255,
			(float) g / 255,
			(float) b / 255
		});
	}
}
