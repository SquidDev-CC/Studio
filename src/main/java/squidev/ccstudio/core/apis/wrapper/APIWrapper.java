package squidev.ccstudio.core.apis.wrapper;

import squidev.ccstudio.core.apis.CCAPIWrapper;

/**
 * Basic wrapper for APIs built with {@see APIBuilder}
 */
public abstract class APIWrapper<T> extends CCAPIWrapper {
	public final T instance;

	public APIWrapper(T inst) {
		instance = inst;
	}
}
