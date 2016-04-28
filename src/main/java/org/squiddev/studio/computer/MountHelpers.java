package org.squiddev.studio.computer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class MountHelpers {
	public static final File mainJar;

	static {
		URL url = null;
		try {
			url = ClassLoader.getSystemClassLoader().loadClass("dan200.computercraft.api.filesystem.IMount")
				.getProtectionDomain().getCodeSource().getLocation();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		File jar;
		try {
			jar = new File(url.toURI());
		} catch (URISyntaxException ignored) {
			jar = new File(url.getPath());
		}

		mainJar = jar;
	}
}