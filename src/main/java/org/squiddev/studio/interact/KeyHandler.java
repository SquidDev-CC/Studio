package org.squiddev.studio.interact;

import org.squiddev.studio.computer.ComputerManager;

import static org.squiddev.studio.interact.KeyMapper.*;

/**
 * Tracks keys which are down
 */
public class KeyHandler {
	/**
	 * Number of ticks before an action occurs.
	 */
	public static final int COMMAND_THRESHOLD = 20;

	private boolean[] keysDown = new boolean[256];
	private boolean[] oldKeysDown = new boolean[256];
	private final ComputerManager manager;

	private int terminateCount = -1;
	private int shutdownCount = -1;
	private int restartCount = -1;

	public KeyHandler(ComputerManager manager) {
		this.manager = manager;
	}

	public boolean keyDown(int key) {
		if (!keysDown[key]) {
			keysDown[key] = true;
			manager.queueEvent("key", key);
			return true;
		}

		return false;
	}

	public void character(char character, boolean ctrlDown, boolean altDown, boolean uppercase) {
		if (!ctrlDown | altDown) {
			manager.queueEvent("char", Character.toString(uppercase ? Character.toUpperCase(character) : character));
		}

		int index = KeyMapper.get(character);
		if (index > 0 && keyDown(index)) handleModifier(character, ctrlDown);
	}

	private void handleModifier(char character, boolean ctrlDown) {
		switch (character) {
			case 't':
				if (ctrlDown) {
					terminateCount++;
				} else {
					terminateCount = -1;
				}
				break;
			case 'r':
				if (ctrlDown) {
					restartCount++;
				} else {
					restartCount = -1;
				}
				break;
			case 's':
				if (ctrlDown) {
					shutdownCount++;
				} else {
					shutdownCount = -1;
				}
				break;

		}
	}


	public void tick() {
		boolean[] keysDown = this.keysDown, oldKeysDown = this.oldKeysDown;

		if (!keysDown[KEY_TERMINATE]) {
			terminateCount = -1;
		} else if (terminateCount >= COMMAND_THRESHOLD) {
			manager.queueEvent("terminate");
			terminateCount = -1;
		}

		if (!keysDown[KEY_SHUTDOWN]) {
			shutdownCount = -1;
		} else if (shutdownCount >= COMMAND_THRESHOLD) {
			manager.shutdown();
			shutdownCount = -1;
		}

		if (!keysDown[KEY_RESTART]) {
			restartCount = -1;
		} else if (restartCount >= COMMAND_THRESHOLD) {
			manager.shutdown();
			restartCount = -1;
		}

		for (int key = 0; key < 256; key++) {
			if (oldKeysDown[key] && !keysDown[key]) manager.queueEvent("key_up", key);

			oldKeysDown[key] = keysDown[key];
			keysDown[key] = false;
		}
	}
}
