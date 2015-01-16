package squidev.ccstudio.core.apis.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Show that this function is a Lua API
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaAPI {
	/**
	 * The names of the Lua API
	 */
	public String[] value() default "";
}
