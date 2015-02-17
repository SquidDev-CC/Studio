package squiddev.ccstudio.core.luaj.profiler;

import org.junit.Test;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.compiler.LuaC;
import squiddev.ccstudio.luaj.PerformanceRunner;

import java.io.IOException;
import java.io.InputStream;

public class ProfilerTest {
	public static void main(String args[]) {
		new ProfilerTest().testProfiler();
	}

	@Test
	public void testProfiler() {
		LuaTable globals = PerformanceRunner.getGlobals();
		LuaC.install();

		InputStream aesStream = PerformanceRunner.class.getResourceAsStream("/squiddev/ccstudio/luaj/aes/AesLua.lua");
		InputStream speedStream = PerformanceRunner.class.getResourceAsStream("/squiddev/ccstudio/luaj/aes/AesSpeed.lua");

		try {
			LoadState.load(aesStream, "AesLua.lua", globals).invoke();
			new Profiler(LoadState.load(speedStream, "AesSpeed.lua", globals)).invoke();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
