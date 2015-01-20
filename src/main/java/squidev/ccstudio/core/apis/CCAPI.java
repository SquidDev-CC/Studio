package squidev.ccstudio.core.apis;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import squidev.ccstudio.computer.Computer;

/**
 * Core CC API
 */
public abstract class CCAPI implements ICCObject {
	protected LuaValue env = LuaValue.NIL;
	protected LuaTable table = null;

	protected Computer computer = null;
	/**
	 * Get the names this API should be stored in
	 */
	public abstract String[] getNames();

	protected abstract LuaTable createTable();

	public LuaTable getTable() {
		if (table == null) {
			table = createTable();
		}
		return table;
	}

	/**
	 * Bind this API to an environment
	 */
	public void bind() {
		String[] names = getNames();
		if (names != null) {
			LuaTable t = getTable();
			for (String name : names) {
				env.set(name, t);
			}
		}
	}

	/**
	 * Get the environment to execute under
	 *
	 * @return The current environment
	 */
	public LuaValue getEnv() {
		return env;
	}

	/**
	 * Get the computer
	 * @return The API's computer
	 */
	public Computer getComputer() {
		return computer;
	}

	/**
	 * Setup the API
	 *
	 * @param computer    The computer to use
	 * @param environment The
	 */
	public void setup(Computer computer, LuaValue environment) {
		if (this.computer != null && this.computer != computer)
			throw new IllegalStateException("Cannot change computer");
		this.computer = computer;

		if (env != environment) {
			// If we change the environment then we need a new table
			env = environment;
			table = null;
		}
	}
}
