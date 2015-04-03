package org.squiddev.ccstudio.core.api;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
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
	 * Create a function with the specified index.
	 * Override to use custom functions
	 *
	 * @param index The function's index
	 * @return The created function
	 */
	@Override
	protected LuaValue createFunction(int index) {
		LuaFunction f = new InvokeFunction(index);
		f.setfenv(env);
		return f;
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
