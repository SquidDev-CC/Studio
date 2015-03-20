package squiddev.ccstudio.core.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A slightly nicer way of handling exceptions
 *
 * I know {@see org.junit.rules.ExpectedException} exists, but it is not as flexible
 */
public class ExpectException {
	/**
	 * Invoke runnable and ensure that it throws an exception
	 *
	 * @param type    The type of exception to check against
	 * @param message The message to check against
	 * @param run     The runnable to call
	 */
	public static void expect(Class<?> type, String message, Runnable... run) {
		for (Runnable r : run) {
			try {
				r.run();
			} catch (Exception e) {
				if (type != null) {
					assertTrue("Expected " + type.getName() + " got " + e.getClass().getName(), type.isInstance(e));
				}

				if (message != null) {
					assertEquals(message, e.getMessage());
				}

				continue;
			}

			if (type == null) {
				assertTrue("Expected exception", false);
			} else {
				assertTrue("Expected exception of type " + type.getName(), false);
			}
		}
	}

	/**
	 * Invoke runnable and ensure that it throws an exception
	 *
	 * @param type The type of exception to check against
	 * @param run  The runnable to call
	 */
	public static void expect(Class<?> type, Runnable... run) {
		expect(type, null, run);
	}

	/**
	 * Invoke runnable and ensure that it throws an exception
	 *
	 * @param message The message to check against
	 * @param run     The runnable to call
	 */
	public static void expect(String message, Runnable... run) {
		expect(null, message, run);
	}
}
