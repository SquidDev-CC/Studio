package org.squiddev.ccstudio.output.lwjgl;

import org.squiddev.ccstudio.computer.Computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles key commands
 */
public class Actions {
	public Map<Integer, List<Action>> actions = new HashMap<>();

	public static final long HOLD_TIME = 1500L;
	public static final long COOLDOWN_TIME = 10L;

	public void add(Action action) {
		List<Action> list = actions.get(action.key);
		if (list == null) {
			list = new ArrayList<>();
			actions.put(action.key, list);
		}

		list.add(action);
	}

	public void press(Computer computer, int key, int modifiers, int type) {
		long current = System.currentTimeMillis();
		List<Action> keys = actions.get(key);
		if (keys == null) return;

		for (Action action : keys) {
			if (action.matches(modifiers)) {
				switch (type) {
					case GLFW_RELEASE:
						action.start = -1;
						action.cooldown = 0;
						break;
					case GLFW_PRESS:
					case GLFW_REPEAT:
						long start = action.start;

						if (start <= -1) {
							action.start = current;
						}

						if (current - start >= action.holdTime && action.cooldown <= 0) {
							System.out.println("Execute");
							action.execute(computer);
							action.start = -1;
							action.cooldown = action.cooldown_time;
						} else {
							action.cooldown--;
						}

						break;
				}
			}
		}
	}

	public abstract static class Action {
		public final int key;
		public final int modifiers;

		public long start = -1;
		public long cooldown = 0;

		public final long holdTime;
		public final long cooldown_time;

		public Action(int key, int modifiers, long hold_time, long cooldown_time) {
			this.key = key;
			this.modifiers = modifiers;
			this.holdTime = hold_time;
			this.cooldown_time = cooldown_time;
		}

		public Action(int key, int modifiers, long holdTime) {
			this(key, modifiers, holdTime, COOLDOWN_TIME);
		}

		public Action(int key, int modifiers) {
			this(key, modifiers, HOLD_TIME, COOLDOWN_TIME);
		}

		public boolean matches(int modifiers) {
			return (this.modifiers & modifiers) == this.modifiers;
		}

		public abstract void execute(Computer computer);
	}
}
