package org.squiddev.studio.computer;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.core.filesystem.JarMount;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

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
		CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new IllegalArgumentException("Failed to get CodeSource for " + clazz);
		}

		URL location = codeSource.getLocation();
		File file;
		try {
			file = new File(location.toURI());
		} catch (URISyntaxException e) {
			file = new File(location.getPath());
		}
		return file;
	}
}
