package org.squiddev.ccstudio.core.api;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.squiddev.ccstudio.computer.Computer;
import org.squiddev.luaj.api.LuaObjectWrapper;

/**
 * A custom instance of {@link LuaObjectWrapper} that
 */
public abstract class CCAPI extends LuaObjectWrapper {
	protected LuaValue env = LuaValue.NIL;

	protected Computer computer = null;

	public CCAPI(Object inst) {
		super(inst);
	}

	/**
	 * Convert this to a Lua table
	 *
	 * @return The API object
	 */
	protected LuaTable createTable() {
		LuaTable table = new LuaTable();
		try {
			for (int i = 0, n = methodNames.length; i < n; i++) {
				// Allow multiple names
				for (String name : methodNames[i]) {
					// Each function should be a different object, even if it is identical.
					LuaFunction f = new InvokeFunction(i);
					f.setfenv(env);
					table.set(name, f);
				}
			}
		} catch (Exception e) {
			throw new LuaError("Bind failed: " + e);
		}
		return table;
	}

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
		bind(getEnv());
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
	 *
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
		if (this.computer != null && this.computer != computer) {
			throw new IllegalStateException("Cannot change computer");
		}
		this.computer = computer;

		if (env != environment) {
			// If we change the environment then we need a new table
			env = environment;
			table = null;
		}
	}

	protected class InvokeFunction extends VarArgFunction {
		public final int index;

		public InvokeFunction(int index) {
			this.index = index;
		}

		/**
		 * We override the invoke function (not onInvoke) to prevent changing the stack
		 */
		@Override
		public Varargs invoke(Varargs varargs) {
			computer.tryAbort();
			return CCAPI.this.invoke(varargs, index);
		}
	}
}
