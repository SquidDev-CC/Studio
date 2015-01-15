package squidev.ccstudio.core.apis;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * Basic wrapper for APIs build with {@see APIBuilder}
 */
public abstract class CCAPI<T> {
	public final T instance;

	protected String[] methodNames = new String[0];
	protected String[] names = new String[0];

	protected LuaTable table;

	public CCAPI(T inst) {
		instance = inst;
	}

	/**
	 * Returns the variables this API should be stored in
	 * @return List of variable names
	 */
	public String[] getNames() {
		return names;
	}

	public abstract Varargs invoke(Varargs args, int index);

	/**
	 * Convert this to a Lua table
	 * @return The API object
	 */
	public LuaTable getTable() {
		if(table == null) {
			table = new LuaTable();
			try {
				for ( int i=0, n=methodNames.length; i<n; i++ ) {
					final int index = i;
					LuaFunction f = new VarArgFunction() {
						public Varargs invoke(Varargs args) {
							return CCAPI.this.invoke(args, index);
						}
					};
					table.set(methodNames[i], f);
				}
			} catch ( Exception e ) {
				throw new LuaError( "bind failed: "+e );
			}
		}
		return table;
	}

	/**
	 * Bind this API to an environment
	 */
	public void bind(LuaValue env) {
		LuaTable t = getTable();
		for(String name : getNames()) {
			env.set(name, t);
		}
	}
}
