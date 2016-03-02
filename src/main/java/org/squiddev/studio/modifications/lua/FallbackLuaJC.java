package org.squiddev.studio.modifications.lua;

import org.squiddev.luaj.luajc.CompileOptions;
import org.squiddev.luaj.luajc.ErrorHandler;
import org.squiddev.luaj.luajc.LuaJC;
import org.squiddev.luaj.luajc.analysis.ProtoInfo;
import org.squiddev.patcher.Logger;
import org.squiddev.studio.modifications.Config;

/**
 * A version of LuaJC that falls back to normal Lua interpretation
 */
public final class FallbackLuaJC {
	public static void install() {
		LuaJC.install(new CompileOptions(
			CompileOptions.PREFIX,
			CompileOptions.THRESHOLD,
			Config.Computer.luaJCVerify,
			handler
		));
	}

	private static final ErrorHandler handler = new ErrorHandler() {
		@Override
		public void handleError(ProtoInfo info, Throwable throwable) {
			Logger.error(
				"There was an error when compiling " + info.loader.filename + info.name + ".\n" +
					"Please report this error message to http://github.com/SquidDev/luaj.luajc\n" +
					info.toString(),
				throwable
			);
		}
	};
}
