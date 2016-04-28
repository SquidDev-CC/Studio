package org.squiddev.studio.interact.laterna;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.*;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorDeviceConfiguration;
import org.squiddev.studio.computer.ComputerManager;
import org.squiddev.studio.interact.TerminalColour;

import java.io.IOException;

/**
 * Terminal that outputs
 */
public class LanternaTerminal {
	/**
	 * Number of frames per tick
	 */
	public static final int SUB_FRAMERATE = 2;

	private static final TextColor[] colors;

	static {
		TerminalColour[] termColors = TerminalColour.values();
		colors = new TextColor[termColors.length];
		for (int i = 0; i < termColors.length; i++) {
			TerminalColour color = termColors[i];
			colors[i] = new TextColor.RGB(color.getR(), color.getG(), color.getB());
		}
	}

	public final ComputerManager manager;
	private final Terminal terminal;
	private final ExtendedTerminal extended;
	private final LanternaKeyHandler keys;

	private TerminalSize size;
	private boolean sizeChanged;

	public LanternaTerminal(final ComputerManager manager) throws IOException {
		this.manager = manager;
		keys = new LanternaKeyHandler(manager);

		terminal = new DefaultTerminalFactory()
			.setInitialTerminalSize(new TerminalSize(manager.terminal.getWidth(), manager.terminal.getHeight()))
			// .setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG)
			.setTerminalEmulatorDeviceConfiguration(new TerminalEmulatorDeviceConfiguration(2000, 500, TerminalEmulatorDeviceConfiguration.CursorStyle.UNDER_BAR, new TextColor.RGB(255, 255, 255), true))
			.setTerminalEmulatorTitle(getLabel())
			.createTerminal();

		sizeChanged = false;

		try {
			size = terminal.getTerminalSize();
		} catch (IOException e) {
			size = new TerminalSize(51, 19);
		}

		manager.terminal.resize(size.getColumns(), size.getRows());

		terminal.addResizeListener(new ResizeListener() {
			@Override
			public void onResized(Terminal terminal, TerminalSize newSize) {
				sizeChanged = true;
				size = newSize;
			}
		});

		if (terminal instanceof ExtendedTerminal) {
			extended = (ExtendedTerminal) terminal;
			System.out.println("Got extended terminal");
		} else {
			extended = null;
			System.out.println("No extended terminal: mouse support is disabled");
		}
	}

	private boolean changed = false;

	private String getLabel() {
		String label = manager.computerInfo.label;
		return (label == null ? "Computer #" + manager.computerInfo.id : label) + " - CCStudio";
	}


	private void updateTerminal() throws IOException {
		if (changed) {
			changed = false;
			if (extended != null) extended.setTitle(getLabel());
		}

		if (manager.pollTerminal()) {
			terminal.setCursorVisible(false);

			dan200.computercraft.core.terminal.Terminal term = manager.terminal;

			char back = '\0', fore = '\0';
			int width = term.getWidth(), height = term.getHeight();

			for (int y = 0; y < height; y++) {
				char[] textBuffer = term.getLine(y).m_text;
				char[] backBuffer = term.getBackgroundColourLine(y).m_text;
				char[] foreBuffer = term.getTextColourLine(y).m_text;

				terminal.setCursorPosition(0, y);
				for (int x = 0; x < width; x++) {
					{
						char b = backBuffer[x];
						if (b != back) {
							terminal.setBackgroundColor(colors[TerminalColour.indexFromCharacter(b)]);
							back = b;
						}
					}

					{
						char f = foreBuffer[x];
						if (f != fore) {
							terminal.setForegroundColor(colors[TerminalColour.indexFromCharacter(f)]);
							fore = f;
						}
					}

					terminal.putCharacter(textBuffer[x]);
				}
			}

			terminal.setCursorPosition(term.getCursorX(), term.getCursorY());
			terminal.flush();
		}

		terminal.setCursorVisible(manager.isBlinking());
		terminal.flush();
	}

	private boolean handleInput(KeyStroke stroke) {
		switch (stroke.getKeyType()) {
			case EOF:
				return true;
			case Character:
				keys.character(stroke.getCharacter(), stroke.isCtrlDown(), stroke.isAltDown(), stroke.isShiftDown());
				return false;
			default: {
				keys.keyDown(stroke.getKeyType());
				return false;
			}
		}
	}

	public void run() throws IOException, InterruptedException {
		boolean isOn = false;
		try {
			terminal.enterPrivateMode();
			terminal.clearScreen();

			while (true) {
				for (int i = 0; i < SUB_FRAMERATE; i++) {
					if (sizeChanged) {
						synchronized (manager.terminal) {
							manager.terminal.resize(size.getColumns(), size.getRows());
							manager.queueEvent("term_resize", size.getColumns(), size.getRows());
							sizeChanged = false;
						}
					}

					KeyStroke stroke = terminal.pollInput();
					while (stroke != null) {
						if (handleInput(stroke)) throw new InterruptedException("Exited");
						stroke = terminal.pollInput();
					}

					updateTerminal();
					Thread.sleep(50 / SUB_FRAMERATE);
				}

				keys.tick();
				changed |= manager.update();
				if (isOn && !manager.isOn()) break;
				isOn = manager.isOn();
			}
		} finally {
			manager.unload();
			terminal.exitPrivateMode();
		}
	}
}
