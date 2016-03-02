package org.squiddev.studio.modifications.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper methods for various things
 */
public class Helpers {
	public static boolean equals(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}

	public static int THREAD_PRIORITY = Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2;

	public static ScheduledExecutorService createThread(String name, int threads) {
		final String prefix = "Studio-" + name + "-";
		final AtomicInteger counter = new AtomicInteger(1);

		SecurityManager manager = System.getSecurityManager();
		final ThreadGroup group = manager == null ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();
		return Executors.newScheduledThreadPool(threads, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(group, runnable, prefix + counter.getAndIncrement());
				if (!thread.isDaemon()) thread.setDaemon(true);
				if (thread.getPriority() != THREAD_PRIORITY) thread.setPriority(THREAD_PRIORITY);

				return thread;
			}
		});
	}
}
