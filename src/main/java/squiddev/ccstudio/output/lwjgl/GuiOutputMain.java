package squiddev.ccstudio.output.lwjgl;

import org.luaj.vm2.LuaValue;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import squiddev.ccstudio.computer.Computer;
import squiddev.ccstudio.core.Config;
import squiddev.ccstudio.output.IOutput;
import squiddev.ccstudio.output.Keys;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GuiOutputMain {

	// We need to strongly reference callback instances. Because this isn't stupid or anything
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;
	private GLFWCharCallback charCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;

	// The window handle
	private long window;

	protected int width;
	protected int height;

	protected Computer computer;

	protected int xPos = -1;
	protected int yPos = -1;
	protected boolean[] mouseDown = new boolean[3];

	public void run() {
		try {
			init();
			loop();

			// Release window and window callbacks
			glfwDestroyWindow(window);

			keyCallback.release();
			charCallback.release();
			mouseButtonCallback.release();

			if (computer != null) computer.shutdown(true);
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
		Keys k = new Keys();
		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
					glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
				} else {
					// We only should queue an event if it is a press or a repeat
					if (computer != null && action > 0) {
						Integer code = k.keys.get(key);

						if (code != null) {
							computer.queueEvent("key", LuaValue.valueOf(code));
						} else {
							System.out.println("Unknown key: " + key + " " + scancode + " " + action + " " + mods);
						}
					}
				}
			}
		});

		glfwSetCharCallback(window, charCallback = new GLFWCharCallback() {
			@Override
			public void invoke(long window, int key) {

				if (computer != null) {
					char letter = (char) key;
					if (letter >= ' ' && letter <= '~') {
						computer.queueEvent("char", LuaValue.valueOf(Character.toString(letter)));
					}
				}
			}
		});

		glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int down, int haventAClue) {
				if (button >= 0 && button <= 3) {
					mouseDown[button] = down == 1;
					if (down == 1) {
						computer.queueEvent("mouse_down", LuaValue.varargsOf(LuaValue.valueOf(button + 1), LuaValue.valueOf(xPos), LuaValue.valueOf(yPos)));
					}
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
		glEnable(GL_TEXTURE_2D);
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
		glShadeModel(GL_SMOOTH);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(0, width, height, 0, 0, 1); // Because top-left is nicer
		glViewport(0, 0, width, height);

		glMatrixMode(GL_MODELVIEW);

		// Set the clear color
		glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		GuiOutput output = new GuiOutput();
		output.setConfig(output.getDefaults());
		computer = new Computer(new Config(), output);
		computer.start();

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (glfwWindowShouldClose(window) == GL_FALSE) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			{
				// Buffers for mouse x and y

				boolean[] mouse = mouseDown;
				boolean setPosition = false;
				boolean changed = false;
				for (int i = 0; i < 3; i++) {
					if (mouse[i]) {
						if (!setPosition) {
							setPosition = true;
							DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
							DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);

							glfwGetCursorPos(window, xBuffer, yBuffer);
							xBuffer.rewind();
							yBuffer.rewind();
							int x = (int) (xBuffer.get() / width * 51) + 1;
							int y = (int) (yBuffer.get() / height * 19) + 1;

							if (x != xPos || y != yPos) {
								changed = true;
								xPos = x;
								yPos = y;
							}
						}

						if (changed) {
							computer.queueEvent("mouse_drag", LuaValue.varargsOf(LuaValue.valueOf(i + 1), LuaValue.valueOf(xPos), LuaValue.valueOf(yPos)));
						}
					}
				}

				if (!setPosition) {
					xPos = -1;
					yPos = -1;
				}

			}

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
