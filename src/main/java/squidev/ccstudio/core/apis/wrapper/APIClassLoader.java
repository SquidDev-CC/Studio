package squidev.ccstudio.core.apis.wrapper;

/**
 * Handles loading generated APIs
 */
class APIClassLoader extends ClassLoader {
	public static final String SUFFIX = "_GenAPI";
	@Override
	protected Class findClass(String name) throws ClassNotFoundException {
		if (name.endsWith(SUFFIX)) {
			// We want to remove the _GenAPI part of the string
			String actualName = name.substring(0, name.length() - SUFFIX.length());

			byte[] bytes = APIBuilder.createAPI(Class.forName(actualName));
			return defineClass(name, bytes, 0, bytes.length);
		}

		return super.findClass(name);
	}
}
