package org.squiddev.studio.interact.laterna;

import com.googlecode.lanterna.input.KeyType;
import org.squiddev.studio.computer.ComputerManager;
import org.squiddev.studio.interact.KeyHandler;

/**
 * A key handler for Lanterna: correctly maps keys
 */
public class LanternaKeyHandler extends KeyHandler {
	public LanternaKeyHandler(ComputerManager manager) {
		super(manager);
	}

	public void keyDown(KeyType type) {
		int key = keyCode(type);
		if (key > 0) keyDown(key);
	}

	private static int[] keyMappings = new int[]{
		-1, // Character
		-1, // Escape
		14, // Backspace
		203,
		205,
		200,
		208,
		210,
		211,
		199,
		207,
		201,
		208,
		15,
		-1, // Reverse tab
		28,
		59, // Function keys
		60,
		61,
		64,
		65, // F5
		66,
		67,
		68,
		79,
		70, // F10
		87, // F11
		88, // F12
		96, // F13
		97, // F14
		98, // F15
	};

	public static int keyCode(KeyType type) {
		int key = type.ordinal();
		return key >= 0 && key < keyMappings.length ? keyMappings[key] : -1;
	}
}
