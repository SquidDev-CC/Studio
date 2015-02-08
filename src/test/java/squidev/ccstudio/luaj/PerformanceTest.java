package squidev.ccstudio.luaj;

import org.junit.Test;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.computer.api.BitAPI;
import squidev.ccstudio.core.Config;
import squidev.ccstudio.core.apis.wrapper.APIClassLoader;
import squidev.ccstudio.core.apis.wrapper.APIWrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Test the performance between LuaC and LuaJC
 */
public class PerformanceTest {
	@Test
	public void testLuaC() {
		LuaTable globals = getGlobals();
		LuaC.install();
		execute("LuaC", globals);
	}

	@Test
	public void testLuaJC() {
		LuaTable globals = getGlobals();
		LuaJC.install();
		execute("LuaJC", globals);
	}

	protected LuaTable getGlobals() {
		LuaTable globals = JsePlatform.debugGlobals();

		APIClassLoader loader = new APIClassLoader();
		APIWrapper wrapper = loader.makeInstance(new BitAPI());
		wrapper.setup(new Computer(new Config()), globals);
		wrapper.bind();

		return globals;
	}

	protected void execute(String name, LuaTable globals) {
		try {
			InputStream aesStream = getClass().getResourceAsStream("/squiddev/ccstudio/luaj/aes/AesLua.lua");
			InputStream speedStream = getClass().getResourceAsStream("/squiddev/ccstudio/luaj/aes/AesSpeed.lua");

			long start = System.nanoTime();
			LuaFunction aes = LoadState.load(aesStream, "AesLua.lua", globals);
			LuaFunction speed = LoadState.load(speedStream, "AesSpeed.lua", globals);

			long compiled = System.nanoTime();

			aes.invoke();
			for (int i = 0; i < 100; i++) {
				speed.invoke();
			}

			long finished = System.nanoTime();

			System.out.printf(name + ": Compilation: %1$f, Running: %2$f\n", (compiled - start) / 1e9, (finished - compiled) / 1e9);
			System.out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
