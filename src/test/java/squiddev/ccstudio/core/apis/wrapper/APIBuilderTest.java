package squiddev.ccstudio.core.apis.wrapper;

import org.junit.BeforeClass;
import org.junit.Test;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;
import squiddev.ccstudio.computer.Computer;
import squiddev.ccstudio.core.Config;
import squiddev.ccstudio.core.apis.wrapper.builder.APIClassLoader;
import squiddev.ccstudio.core.apis.wrapper.builder.APIWrapper;
import squiddev.ccstudio.core.testutils.ExpectException;
import squiddev.ccstudio.output.terminal.TerminalOutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class APIBuilderTest {
	private static LuaTable table;
	private static LuaTable env;

	@BeforeClass
	public static void testCreateAPI() throws Exception {
		APIClassLoader loader = new APIClassLoader();
		Class<?> wrapped = loader.makeClass(EmbedClass.class);

		APIWrapper api = (APIWrapper) wrapped.getConstructor(EmbedClass.class).newInstance(new EmbedClass());

		// Set environment and bind to a variable
		env = JsePlatform.debugGlobals();
		api.setup(new Computer(new Config(), new TerminalOutput()), env);
		api.bind();

		table = api.getTable();
	}

	/**
	 * Test that functions return what the are meant to
	 */
	@Test
	public void testFunctions() {
		assertEquals(LuaValue.NONE, table.get("noArgsNoReturn").invoke());
		assertEquals(LuaValue.TRUE, table.get("noArgsLuaReturn").invoke());

		assertEquals(2, table.get("varArgsLuaReturn").invoke(LuaValue.valueOf(2)).toint(1));
		assertEquals(2, table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1), LuaValue.valueOf(1)).todouble(1), 0);
	}

	/**
	 * Test that annotations work
	 */
	@Test
	public void testAnnotation() {
		assertTrue(table.get("varArgsLuaReturn").isfunction());
		assertTrue(table.get("one").isfunction());
		assertTrue(table.get("two").isfunction());

		assertEquals(table, env.get("embedded"));
		assertEquals(table, env.get("embed"));
	}

	/**
	 * Test that the correct exceptions are thrown
	 */
	@Test
	public void testErrors() {
		ExpectException.expect(LuaError.class, "Expected number, number",
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(true), LuaValue.valueOf(1)),
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf("HELLO"), LuaValue.valueOf(1)),
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1), LuaValue.valueOf(1.12)),
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1))
		);

		ExpectException.expect(LuaError.class, "I expected better of you!",
			() -> table.get("testingError").invoke(LuaValue.valueOf(true), LuaValue.valueOf(1))
		);
	}

	/**
	 * Test that subargs for varargs works
	 */
	@Test
	public void testSubArgs() {
		Varargs result = table.get("subArgs").invoke(new LuaValue[]{
			LuaValue.valueOf("2"), LuaValue.valueOf(3), // Normal args
			LuaValue.valueOf("Hello"), LuaValue.valueOf("World") // Subargs
		});

		assertEquals(result.arg(1).toint(), 3);
		assertEquals(result.arg(2).toint(), 2);
		assertEquals(result.arg(3).toString(), "Hello");
		assertEquals(result.arg(4).toString(), "World");
	}

	@SuppressWarnings("UnusedDeclaration")
	@LuaAPI({"embedded", "embed"})
	public static class EmbedClass {
		@LuaFunction
		public void noArgsNoReturn() {
		}

		@LuaFunction
		public double twoArgsOneReturn(double a, int b) {
			return a + b;
		}

		@LuaFunction(error = "I expected better of you!")
		public double testingError(double a, int b) {
			return a + b;
		}

		@LuaFunction
		public LuaValue noArgsLuaReturn() {
			return LuaValue.TRUE;
		}

		@LuaFunction(value = {"one", "two", "varArgsLuaReturn"})
		public Varargs varArgsLuaReturn(Varargs args) {
			return args;
		}

		@LuaFunction
		public Varargs subArgs(int a, LuaNumber b, Varargs args) {
			return LuaValue.varargsOf(b, LuaValue.valueOf(a), args);
		}
	}
}
