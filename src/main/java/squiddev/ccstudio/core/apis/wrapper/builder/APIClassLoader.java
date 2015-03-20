package squiddev.ccstudio.core.apis.wrapper.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading generated APIs
 */
public class APIClassLoader extends ClassLoader {
	public static final String SUFFIX = "_GenAPI";
	private static final Map<Class<?>, Class<?>> cache = new HashMap<>();

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (name.endsWith(SUFFIX)) {
			// We want to remove the _GenAPI part of the string
			return createClass(name, Class.forName(name.substring(0, name.length() - SUFFIX.length())));
		}

		return super.findClass(name);
	}

	/**
	 * Make a class based of a class
	 *
	 * @param rootClass The class to base it of
	 * @return The wrapper class
	 */
	public Class<?> makeClass(Class<?> rootClass) {
		return createClass(rootClass.getName() + SUFFIX, rootClass);
	}

	/**
	 * Make an instance of the wrapper
	 *
	 * @param rootInstance The class instance to base it off
	 * @return The resulting instance
	 */
	public APIWrapper makeInstance(Object rootInstance) {
		Class<?> rootClass = rootInstance.getClass();
		Class<?> wrapper = makeClass(rootClass);
		try {
			return (APIWrapper) wrapper.getConstructor(rootClass).newInstance(rootInstance);
		} catch (ReflectiveOperationException e) {
			// This should NEVER happen. We've made this class, so we should never get any errors
			throw new RuntimeException("Cannot create API", e);
		}
	}

	/**
	 * Attempt to load the class from the cache,
	 * and if not then create it
	 *
	 * @param name     The name of the class to create
	 * @param original The original class to base it off
	 * @return The created class
	 */
	protected Class<?> createClass(String name, Class<?> original) {
		Class<?> result = cache.get(original);
		if (result == null) {
			byte[] bytes = new APIBuilder(original).toByteArray();
			result = defineClass(name, bytes, 0, bytes.length);
			cache.put(original, result);
		}

		return result;
	}
}
