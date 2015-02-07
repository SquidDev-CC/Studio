package squidev.ccstudio.luaj;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJCWorking;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class JavaBuilderTest {
	/**
	 * Fetch a list of tests to run
	 *
	 * @return Array of parameters to run. Each array is composed of one element with the name of the test
	 */
	@Parameterized.Parameters(name = "{0} ({index})")
	public static Collection<Object[]> getLua() {
		Object[][] tests = {
			{"NForLoop"},
			{"WhileLoop"},
			{"DoBlock"},
			{"Function"},
		};

		return Arrays.asList(tests);
	}

	protected String name;
	protected LuaValue globals;

	public JavaBuilderTest(String name) {
		globals = JsePlatform.debugGlobals();
		globals.set("assertEquals", new AssertFunction());
		LuaJCWorking.install();

		this.name = name;
	}

	/**
	 * Get the Lua test and run it
	 */
	@Test
	public void runLua() throws Exception {
		LoadState.load(getClass().getResourceAsStream("/squiddev/ccstudio/luaj/" + name + ".lua"), "test-" + name, globals).invoke();
	}

	class AssertFunction extends ThreeArgFunction {
		@Override
		public LuaValue call(LuaValue expected, LuaValue actual, LuaValue message) {
			String msg = message.toString();
			if (message.isnil()) {
				msg = "(No message)";
			}

			assertEquals(msg, expected.typename(), actual.typename());
			assertEquals(msg, expected.tojstring(), expected.tojstring());

			return LuaValue.NONE;
		}
	}
}
