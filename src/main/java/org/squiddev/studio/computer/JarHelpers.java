package org.squiddev.studio.computer;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.core.filesystem.JarMount;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Various helpers for getting files from a Jar file
 */
public final class JarHelpers {
	private JarHelpers() {
	}

	public static IMount createResourceMount(Class<?> modClass, String domain, String subPath) {
		try {
			File jar = getJarFromClass(modClass);
			return new JarMount(jar, "assets/" + domain + "/" + subPath);
		} catch (IOException localIOException1) {
		}
		return null;
	}

	public static File getJarFromClass(Class<?> clazz) {
		String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
		int bangIndex = path.indexOf("!");
		if (bangIndex >= 0) {
			path = path.substring(0, bangIndex);
		}

		URL url;
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			return null;
		}

		File file;
		try {
			file = new File(url.toURI());
		} catch (URISyntaxException e) {
			file = new File(url.getPath());
		}

		return file;
	}
}
