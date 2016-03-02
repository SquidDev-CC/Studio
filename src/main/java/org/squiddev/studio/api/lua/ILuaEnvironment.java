package org.squiddev.studio.api.lua;

/**
 * Various hooks and methods for interfacing with the Lua environment
 */
public interface ILuaEnvironment {
	/**
	 * Register a custom API
	 *
	 * @param factory The API factory to register
	 */
	void registerAPI(ILuaAPIFactory factory);
}
