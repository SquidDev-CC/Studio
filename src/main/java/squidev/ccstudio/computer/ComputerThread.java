package squidev.ccstudio.computer;

/**
 * Runs a computer
 */
public class ComputerThread implements Runnable {
	public final Computer computer;

	public ComputerThread(Computer computer) {
		this.computer = computer;
	}

	@Override
	public void run() {

	}
}
