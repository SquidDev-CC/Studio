package org.squiddev.studio.api;

import org.squiddev.studio.api.lua.ILuaEnvironment;

/**
 * Initialise a module
 */
public interface Module {
	/**
	 * Setup the transformer
	 *
	 * @param transformer The transformer to setup
	 */
	void setupTransformer(Transformer transformer);

	/**
	 * Setup the Lua environment
	 *
	 * @param environment The lua environment
	 */
	void setupLua(ILuaEnvironment environment);
}
