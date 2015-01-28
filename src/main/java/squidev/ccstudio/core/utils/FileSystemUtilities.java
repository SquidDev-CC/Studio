package squidev.ccstudio.core.utils;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.core.filesystem.JarMount;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Handles utilities to get mounts
 */
public class FileSystemUtilities {
	public static IMount getJarRomMount() {
		String path = IMount.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		int bangIndex = path.indexOf("!");
		if (bangIndex >= 0) path = path.substring(0, bangIndex);

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

		try {
			return new JarMount(file, "assets/computercraft/lua/rom");
		} catch (IOException e) {
			return null;
		}
	}

	public static InputStream biosStream() {
		return IMount.class.getResourceAsStream("/assets/computercraft/lua/bios.lua");
	}
}
