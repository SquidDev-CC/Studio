package org.squiddev.studio.interact.laterna;

import dan200.computercraft.core.computer.ComputerThread;
import org.squiddev.studio.api.Transformer;
import org.squiddev.studio.computer.ComputerManager;
import org.squiddev.studio.modifications.Loader;
import org.squiddev.studio.storage.ComputerInfo;
import org.squiddev.studio.storage.Session;

/**
 * Runs a computer
 */
public class Runner {
	public static void run(Transformer transformer, String[] args) {
		new Loader(transformer).setup();

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
