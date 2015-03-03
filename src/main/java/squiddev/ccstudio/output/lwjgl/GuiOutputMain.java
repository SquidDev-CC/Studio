package squiddev.ccstudio.output.lwjgl;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import squiddev.ccstudio.output.IOutput;

import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GuiOutputMain {

	// We need to strongly reference callback instances. Because this isn't stupid or anything
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;

	// The window handle
	private long window;

	protected int width;
	protected int height;

	public void run() {
		try {
			init();
			loop();

			// Release window and window callbacks
			glfwDestroyWindow(window);

			keyCallback.release();
		} finally {
			// Terminate GLFW and release the GLFWerrorfun
			glfwTerminate();
			errorCallback.release();
		}
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (glfwInit() != GL11.GL_TRUE) throw new IllegalStateException("Unable to initialize GLFW");

		// Setup a window, we want it to be hidden whilst we set things up
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

		width = IOutput.WIDTH * 12;
		height = IOutput.HEIGHT * 18;

		String label = "Meh";
		// String label = computer.environment.label;
		// if (label == null || label.length() == 0) label = "Computer #" + computer.environment.id;

		// Create the window
		window = glfwCreateWindow(width, height, label, NULL, NULL);
		if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
					glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
				} else {
					System.out.println(key + " " + scancode + " " + action + " " + mods);
				}
			}
		});

		// Get the resolution of the primary monitor
		ByteBuffer videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		// Center our window
		glfwSetWindowPos(
				window,
				(GLFWvidmode.width(videoMode) - width) / 2,
				(GLFWvidmode.height(videoMode) - height) / 2
		);

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);

		// Enable v-sync
		glfwSwapInterval(1);

		GLContext.createFromCurrent();

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(0, width, height, 0, 0, 1); // Because top-left is nicer
		glViewport(0, 0, width, height);
//		glOrtho(0, width, 0, height, 1, -1);

		glMatrixMode(GL_MODELVIEW);

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		GuiOutput output = new GuiOutput();
		output.setConfig(output.getDefaults());
		output.setBackColor(2);
		output.clear();
		output.setBackColor(1);
		output.write("HELLO".getBytes());
//		Computer computer = new Computer(new Config(), output);
//		computer.start();

		while (glfwWindowShouldClose(window) == GL_FALSE) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			output.redraw();

			glfwSwapBuffers(window); // swap the color buffers*/

			glfwPollEvents();
		}
	}

	public static void main(String[] args) {
		setPath();

		new GuiOutputMain().run();
	}

	protected static void setPath() {
		String os = System.getProperty("os.name");
		if (os == null) throw new IllegalArgumentException("Cannot guess OS and java.library.path is not set");
		os = os.toLowerCase();

		String bitType = System.getProperty("sun.arch.data.model");
		switch (bitType) {
			case "64":
				bitType = "x64";
				break;
			case "32":
				bitType = "x86";
				break;
			default:
				throw new IllegalArgumentException("Cannot load natives for " + bitType);
		}

		String nativePath;
		if (os.contains("win")) {
			nativePath = "windows";
		} else if (os.contains("mac")) {
			if (bitType.equals("x86")) throw new IllegalArgumentException("Cannot load natives for x86");
			nativePath = "macosx";
		} else if (os.contains("nix") | os.contains("nux") | os.contains("aix")) {
			nativePath = "linux";
		} else {
			throw new IllegalArgumentException("Cannot load natives for " + os);
		}

		String folder = new File(nativePath, bitType).toString();

		String path = File.pathSeparator + new File(new File(System.getProperty("user.dir"), "native"), folder).toString();

		String nativesPath = System.getProperty("squiddev.natives");
		if (nativesPath != null) {
			path += File.pathSeparator + new File(nativesPath, folder).toString();
		}

		System.setProperty("java.library.path", System.getProperty("java.library.path") + path);

		System.out.println("Setting natives to " + path);
	}

}
