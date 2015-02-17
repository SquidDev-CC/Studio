package squiddev.ccstudio.core.luaj.profiler;

import org.luaj.vm2.LuaValue;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;

/**
 * Handles wrapping the profiler object
 */
@LuaAPI("profile")
public class ProfilerAPI {
	@LuaFunction
	public void profile(LuaValue value) {
	}
}
