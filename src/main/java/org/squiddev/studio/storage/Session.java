package org.squiddev.studio.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared state across computers
 */
public class Session {
	@Exclude
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

	@Exclude
	public final List<ComputerInfo> computers = new ArrayList<ComputerInfo>();

	public int newId() {
		synchronized (lock) {
			return lastId++;
		}
	}
}
