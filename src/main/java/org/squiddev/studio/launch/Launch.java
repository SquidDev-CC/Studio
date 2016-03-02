package org.squiddev.studio.launch;

import org.squiddev.patcher.Logger;
import org.squiddev.studio.api.Transformer;

import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * The main launcher
 */
public class Launch {
	public static void main(String[] args) {
		String type = "laterna";
		if (args.length > 0) type = args[0];

		URLClassLoader current = (URLClassLoader) Launch.class.getClassLoader();
		RewritingLoader classLoader = new RewritingLoader(current.getURLs());
		Thread.currentThread().setContextClassLoader(classLoader);

		try {
			classLoader.loadClass("org.squiddev.studio.interact." + type + ".Runner")
				.getMethod("run", Transformer.class, String[].class)
				.invoke(null, classLoader.chain, args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : args);
		} catch (Exception e) {
			Logger.error("Unknown exception", e);
			System.exit(1);
		}
	}
}
