package squidev.ccstudio.core.apis.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The name of the lua function
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaFunction {
	/**
	 * The name of the Lua Function, defaults to the actual function name
	 * @return
	 */
	public String name() default "";

	/**
	 * If this function returns multiple values
	 * @return
	 */
	public boolean multiReturn() default false;
}
