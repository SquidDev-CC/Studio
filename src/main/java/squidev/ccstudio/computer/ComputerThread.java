package squidev.ccstudio.computer;

import squidev.ccstudio.core.Config;

import java.util.concurrent.BlockingQueue;

/**
 * Runs a computer
 */
public class ComputerThread {
	/**
	 * The computer for this thread
	 */
	public final Computer computer;
	/**
	 * The lock to work with to ensure synchronisation
	 */
	private final Object lock = new Object();
	/**
	 * The state the computer is in
	 */
	private State state;
	/**
	 * The thread
	 */
	private Thread thread = null;

	public ComputerThread(Computer computer) {
		this.computer = computer;
		state = State.STOPPED;
	}

	public void start() {
		synchronized (lock) {
			switch (state) {
				case RUNNING:
					// Cannot start again
					return;
				case STOPPING:
					// Prevent stopping the thread
					state = State.RUNNING;
					return;
				case STOPPED:
					// Create a new thread
					thread = new Thread(new ComputerInternalThread());
					thread.start();
					state = State.RUNNING;
					return;
				default:
					throw new IllegalArgumentException("Unexpected ComputerThread State");
			}
		}
	}

	/**
	 * Get the state of the thread
	 *
	 * @return The current thread
	 */
	public State getState() {
		return state;
	}

	public enum State {
		/**
		 * Running
		 */
		RUNNING,

		/**
		 * About to stop - will stop after the current event
		 */
		STOPPING,

		/**
		 * Stopped, not processing events
		 */
		STOPPED,
	}

	protected class ComputerInternalThread implements Runnable {
		public static final String yieldingMessage = "Too long without yielding";

		@Override
		public void run() {
			final Computer comp = computer;
			final Config config = comp.config;
			final Config.TooLongYielding timeoutStyle = config.timeoutStyle;
			final long timeoutLength = config.timeoutLength;
			final long timeoutAbortLength = config.timeoutAbortLength;

			final BlockingQueue<Runnable> events = computer.events;

			while (true) {
				synchronized (lock) {
					if (state == State.STOPPING) {
						state = State.STOPPED;
						thread = null;
						return;
					}
				}

				try {
					Thread worker = new Thread(events.take());
					worker.start();
					switch (timeoutStyle) {
						case NONE:
							break;
						case SOFT:
							worker.join(timeoutLength);
							while (worker.isAlive()) {
								comp.messages.add(yieldingMessage);
								worker.join(timeoutLength);
							}
							break;
						case HARD:
							comp.softAbort(yieldingMessage);
							worker.join(timeoutAbortLength);

							if (worker.isAlive()) {
								comp.hardAbort(yieldingMessage);
								worker.join(timeoutAbortLength);

								if (worker.isAlive()) {
									worker.interrupt();
								}
							}


							break;
					}
				} catch (Exception e) {
				}
			}
		}
	}


}
