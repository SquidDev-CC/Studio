package org.squiddev.studio.computer;

import org.squiddev.studio.storage.ComputerInfo;
import org.squiddev.studio.storage.Session;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages a list of computers
 */
public class SessionManager {
	private final Session session;
	private Set<ComputerManager> computers = new HashSet<ComputerManager>();
	private final Object lock = new Object();

	public SessionManager(Session session) {
		this.session = session;
	}

	public void unload() {
		synchronized (lock) {
			for (ComputerManager computer : computers) {
				computer.unload();
			}
		}
	}

	public ComputerManager add(ComputerInfo info) {
		synchronized (lock) {
			ComputerManager manager = new ComputerManager(session, info);
			computers.add(manager);
			return manager;
		}
	}

	public void remove(ComputerManager manager) {
		synchronized (lock) {
			if (computers.remove(manager)) {
				manager.unload();
			}
		}
	}

	public void update() {
		synchronized (lock) {
			for (ComputerManager computer : computers) {
				computer.update();
			}
		}
	}
}
