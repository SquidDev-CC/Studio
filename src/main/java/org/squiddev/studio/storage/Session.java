package org.squiddev.studio.storage;

import dan200.computercraft.shared.util.IDAssigner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared state across computers
 */
public class Session {
	private final Object lock = new Object();
	public int lastId;

	public File directory;

	/**
	 * Current day
	 */
	public int day;

	/**
	 * Time of day
	 */
	public double time;

	public final List<ComputerInfo> computers = new ArrayList<ComputerInfo>();

	public int newId() {
		synchronized (lock) {
			return lastId++;
		}
	}

	public File getSaveDirectory(String name) {
		File location = new File(directory, name);
		int id = IDAssigner.getNextIDFromDirectory(location);

		return new File(location, Integer.toString(id));
	}
}
