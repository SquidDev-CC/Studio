package org.squiddev.studio.storage;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Handles loading and storing classes
 */
public class StorageHandler {
	private static final ExclusionStrategy lockAndExclude = new ExclusionStrategy() {
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return f.getName().equals("lock") || f.getAnnotation(Exclude.class) != null;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return clazz.isAnnotationPresent(Exclude.class);
		}
	};

	private final File root;

	private final Gson builder = new GsonBuilder()
		.setExclusionStrategies(lockAndExclude)
		.create();

	public StorageHandler(File root) {
		this.root = root;
	}

	public File getRoot() {
		return root;
	}

	public void save(Object object, String name) throws IOException {
		Writer writer = null;
		try {
			writer = new FileWriter(new File(root, name));
			builder.toJson(object, writer);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	public <T> T load(Class<T> klass, String name) throws IOException {
		Reader reader = null;
		try {
			reader = new FileReader(new File(root, name));
			return builder.fromJson(reader, klass);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		}
	}
}
