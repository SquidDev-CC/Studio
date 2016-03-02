package org.squiddev.studio.launch;

import com.google.common.io.ByteStreams;
import org.squiddev.patcher.Logger;
import org.squiddev.studio.Config;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Loads classes, rewriting them
 */
public class RewritingLoader extends URLClassLoader {
	private ClassLoader parent = getClass().getClassLoader();

	public final CustomChain chain = new CustomChain();

	private Set<String> classLoaderExceptions = new HashSet<String>();
	private final File dumpFolder;

	public RewritingLoader(URL[] urls) {
		super(urls, null);

		dumpFolder = new File("asm-studio");
		if (Config.Testing.dumpAsm && !dumpFolder.exists() && !dumpFolder.mkdirs()) {
			Logger.warn("Cannot create ASM dump folder");
		}

		// classloader exclusions
		addClassLoaderExclusion("java.");
		addClassLoaderExclusion("sun.");
		addClassLoaderExclusion("org.objectweb.asm.");
		addClassLoaderExclusion("com.google.common.");
		addClassLoaderExclusion("org.squiddev.patcher.");

		addClassLoaderExclusion("org.squiddev.studio.api.Transformer");
		addClassLoaderExclusion("org.squiddev.studio.Config");
		addClassLoaderExclusion("org.squiddev.studio.launch.");
	}

	@Override
	public Class<?> findClass(final String name) throws ClassNotFoundException {
		for (final String exception : classLoaderExceptions) {
			if (name.startsWith(exception)) {
				return parent.loadClass(name);
			}
		}

		for (final String exception : chain.exclusions) {
			if (name.startsWith(exception)) {
				return super.findClass(name);
			}
		}

		try {
			final int lastDot = name.lastIndexOf('.');
			final String fileName = name.replace('.', '/') + ".class";
			URLConnection urlConnection = findCodeSourceConnectionFor(fileName);

			CodeSigner[] signers = null;
			if (lastDot > -1) {
				if (urlConnection instanceof JarURLConnection) {
					final JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
					final JarFile jarFile = jarURLConnection.getJarFile();

					if (jarFile != null && jarFile.getManifest() != null) {
						signers = jarFile.getJarEntry(fileName).getCodeSigners();
					}
				}
			}

			byte[] original = getClassBytes(fileName);
			byte[] transformed = chain.transform(name, original);
			if (transformed != original) writeDump(fileName, transformed);

			final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
			return defineClass(name, transformed, 0, transformed.length, codeSource);
		} catch (Throwable e) {
			throw new ClassNotFoundException(name, e);
		}
	}

	private URLConnection findCodeSourceConnectionFor(final String name) {
		final URL resource = findResource(name);
		if (resource != null) {
			try {
				return resource.openConnection();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	public void addClassLoaderExclusion(String toExclude) {
		classLoaderExceptions.add(toExclude);
	}

	public byte[] getClassBytes(String name) throws IOException {
		InputStream classStream = null;
		try {
			final URL classResource = findResource(name);
			if (classResource == null) return null;

			classStream = classResource.openStream();
			return ByteStreams.toByteArray(classStream);
		} finally {
			if (classStream != null) {
				try {
					classStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	public void writeDump(String fileName, byte[] bytes) {
		if (Config.Testing.dumpAsm) {
			File file = new File(dumpFolder, fileName);
			File directory = file.getParentFile();
			if (directory.exists() || directory.mkdirs()) {
				try {
					OutputStream stream = new FileOutputStream(file);
					try {
						stream.write(bytes);
					} catch (IOException e) {
						Logger.error("Cannot write " + file, e);
					} finally {
						stream.close();
					}
				} catch (FileNotFoundException e) {
					Logger.error("Cannot write " + file, e);
				} catch (IOException e) {
					Logger.error("Cannot write " + file, e);
				}
			} else {
				Logger.warn("Cannot create folder for " + file);
			}
		}
	}
}
