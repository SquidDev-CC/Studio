package org.squiddev.studio.modifications;

import org.squiddev.studio.modifications.lua.socket.AddressMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The main config class
 */
public final class Config {
	public static Set<String> globalWhitelist;

	public static AddressMatcher socketWhitelist;
	public static AddressMatcher socketBlacklist;

	public static void onSync() {

		globalWhitelist = new HashSet<String>(Arrays.asList(Computer.globalWhitelist));

		socketWhitelist = new AddressMatcher(APIs.Socket.whitelist);
		socketBlacklist = new AddressMatcher(APIs.Socket.blacklist);
	}

	/**
	 * Computer tweaks and items.
	 */
	public static final class Computer {
		/**
		 * Globals to whitelist (are not set to nil).
		 * This is NOT recommended for servers, use at your own risk.
		 */
		public static String[] globalWhitelist = new String[]{"debug"};

		/**
		 * Time in milliseconds before 'Too long without yielding' errors.
		 * You cannot shutdown/reboot the computer during this time.
		 * Use carefully.
		 */
		public static int computerThreadTimeout = 7000;

		/**
		 * Compile Lua bytecode to Java bytecode.
		 * This speeds up code execution.
		 */
		public static boolean luaJC = false;

		/**
		 * Verify LuaJC sources on generation.
		 * This will slow down compilation.
		 * If you have errors, please turn this and debug on and
		 * send it with the bug report.
		 */
		public static boolean luaJCVerify = false;
	}

	/**
	 * Custom APIs for computers
	 */
	public static final class APIs {
		/**
		 * TCP connections from the socket API
		 */
		public static final class Socket {
			/**
			 * Enable TCP connections.
			 * When enabled, the socket API becomes available on
			 * all computers.
			 */
			public static boolean enabled = true;

			/**
			 * Blacklisted domain names.
			 *
			 * Entries are either domain names (www.example.com) or IP addresses in
			 * string format (10.0.0.3), optionally in CIDR notation to make it easier
			 * to define address ranges (1.0.0.0/8). Domains are resolved to their
			 * actual IP once on startup, future requests are resolved and compared
			 * to the resolved addresses.
			 */
			public static String[] blacklist = new String[]{"127.0.0.0/8", "10.0.0.0/8", "192.168.0.0/16", "172.16.0.0/12"};

			/**
			 * Whitelisted domain names.
			 * If something is mentioned in both the blacklist and whitelist then
			 * the blacklist takes priority.
			 */
			public static String[] whitelist = new String[0];

			/**
			 * Maximum TCP connections a computer can have at any time
			 */
			public static int maxTcpConnections = 4;

			/**
			 * Number of threads to use for processing name lookups.
			 */
			public static int threads = 4;

			/**
			 * Maximum number of characters to read from a socket.
			 */
			public static int maxRead = 2048;
		}

		/**
		 * Basic data manipulation
		 */
		public static final class Data {
			/**
			 * If the data API is enabled
			 */
			public static boolean enabled = true;

			/**
			 * Maximum number of bytes to process.
			 * The default is 1MiB
			 */
			public static int limit = 1048576;
		}
	}

	/**
	 * Only used when testing and developing the mod.
	 * Nothing to see here, move along...
	 */
	public static final class Testing {
		/**
		 * Dump the modified class files to asm-studio
		 */
		public static boolean dumpAsm = true;
	}
}
