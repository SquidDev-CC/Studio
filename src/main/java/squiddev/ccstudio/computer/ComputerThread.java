package squiddev.ccstudio.computer;

import squiddev.ccstudio.core.Config;

import java.util.concurrent.BlockingQueue;

/**
 * Runs a computer, handling yielding and what not
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

	/**
	 * Start the thread or prevent stopping
	 */
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
					// thread.setDaemon(true);
					thread.start();
					state = State.RUNNING;
					return;
				default:
					throw new IllegalArgumentException("Unexpected ComputerThread State");
			}
		}
	}

	/**
	 * Prepare the thread for stopping
	 *
	 * @param terminate Terminate the thread
	 */
	public void stop(boolean terminate) {
		synchronized (lock) {
			switch (state) {
				case RUNNING:
				case STOPPING:
					state = State.STOPPING;
					if (terminate) {
						thread.interrupt();
					}
					return;
				case STOPPED:
					// Prevent stopping the thread
					return;
				default:
					throw new IllegalArgumentException("Unexpected ComputerThread State");
			}
		}
	}

	/**
	 * Stop the thread
	 */
	public void stop() {
		stop(false);
	}

	/**
	 * Interrupt the thread and stop it
	 */
	public void terminate() {
		stop(true);
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
			final Computer computer = ComputerThread.this.computer;
			final Config config = computer.config;
			final Config.TooLongYielding timeoutStyle = config.timeoutStyle;
			final long timeoutLength = config.timeoutLength;
			final long timeoutAbortLength = config.timeoutAbortLength;

			final BlockingQueue<Runnable> events = ComputerThread.this.computer.events;

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
					worker.setDaemon(true);
					worker.start();
					switch (timeoutStyle) {
						case NONE:
							break;
						case SOFT:
							worker.join(timeoutLength);
							while (worker.isAlive()) {
								// Whilst the computer is alive we just keep pushing messages every time
								// it takes too long
								computer.messages.add(yieldingMessage);
								worker.join(timeoutLength);
							}
							break;
						case HARD:
							worker.join(timeoutLength);
							if (worker.isAlive()) {
								computer.softAbort(yieldingMessage);
								worker.join(timeoutAbortLength);

								if (worker.isAlive()) {
									computer.hardAbort(yieldingMessage);
									worker.join(timeoutAbortLength);

									if (worker.isAlive()) {
										worker.interrupt();
									}
								}
							}
					}
				} catch (InterruptedException e) {
					// Ignore this
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
