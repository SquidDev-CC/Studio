package org.squiddev.studio.interact.laterna;

import java.io.PrintWriter;
import java.io.IOException;

import dan200.computercraft.core.computer.ComputerThread;
import org.squiddev.studio.computer.ComputerManager;
import org.squiddev.studio.storage.ComputerInfo;
import org.squiddev.studio.storage.Session;

/**
 * Runs a computer
 */
public class Runner {
	public static void main(String[] args) {
		Session session = new Session();
		ComputerInfo info = new ComputerInfo();
		session.computers.add(info);

		ComputerManager computer = new ComputerManager(session, info);

		try {
			new LanternaTerminal(computer).run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Exited. Stopping");

		ComputerThread.stop();
		// Force an abort of the computer thread
		System.exit(0);
	}
}
