package squiddev.ccstudio.core.apis.wrapper.builder;

import squiddev.ccstudio.core.apis.CCAPIWrapper;

/**
 * Basic wrapper for APIs built with {@see APIBuilder}
 */
public abstract class APIWrapper<T> extends CCAPIWrapper {
	public final T instance;

	public APIWrapper(T inst) {
		instance = inst;
	}
}
