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
	 * If this function returns multiple values
	 */
	public boolean multiReturn() default false;

	public String error() default "";
}
