package squiddev.ccstudio.core.apis.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Show that this function is a lua function
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaFunction {
	/**
	 * The names of the Lua Function, defaults to the actual function name
	 */
	public String[] value() default "";

	/**
	 * If this function returns multiple values (returns a {@link org.luaj.vm2.Varargs}
	 * By default a {@link org.luaj.vm2.LuaTable} is returned if an array is returned
	 */
	public boolean isVarArgs() default false;

	public String error() default "";
}
