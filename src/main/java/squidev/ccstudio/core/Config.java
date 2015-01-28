package squidev.ccstudio.core;

import java.util.HashSet;
import java.util.Set;

public class Config {
	/**
	 * Verify ASM sources
	 */
	public static boolean verifySources = true;

	/**
	 * Support using the LuaJC compiler
	 *
	 * @see org.luaj.vm2.luajc.LuaJC
	 */
	public boolean useLuaJC = true;

	/**
	 * Globals that are blacklisted
	 */
	public Set<String> blacklist = new HashSet<>();

	/**
	 * Number of instructions to execute before attempting to yield
	 */
	public int coroutineHookCount = 100000;

	/**
	 * How to handle 'Too long without yielding' errors
	 */
	public TooLongYielding timeout = TooLongYielding.HARD;

	/**
	 * Limit of storage
	 */
	public long computerSpaceLimit = 1000000;

	/**
	 * Directory to store computers in
	 */
	public String computerDirectory = "computer";

	public Config() {
		blacklist.add("collectgarbage");
		blacklist.add("dofile");
		blacklist.add("load");
		blacklist.add("loadfile");

		// I know some people think we should have these.
		// However, it would mean hacking LuaJ
		blacklist.add("module");
		blacklist.add("require");
		blacklist.add("package");

		// Allow access to files & etc...
		blacklist.add("io");
		blacklist.add("os");
		blacklist.add("luajava");

		// This prints to StdOut - obviously not good.
		blacklist.add("print");

		blacklist.add("debug");
		blacklist.add("newproxy");
	}

	/**
	 * Too long without yielding handling
	 */
	public static enum TooLongYielding {
		/**
		 * Don't abort the computer, just push a notification
		 */
		SOFT,

		/**
		 * Handle like ComputerCraft does
		 */
		HARD,

		/**
		 * Ignore entirely, take no action
		 */
		NONE,
	}
}
