package squidev.ccstudio.core.apis.wrapper;

import org.junit.BeforeClass;
import org.junit.Test;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.Config;
import squidev.ccstudio.core.testutils.ExpectException;

import static org.junit.Assert.assertEquals;

public class APIBuilderTest {
	private static LuaTable table;
	private static LuaTable env;

	@BeforeClass
	public static void testCreateAPI() throws Exception {
		APIClassLoader loader = new APIClassLoader();
		Class<?> wrapped = loader.makeClass(EmbedClass.class);

		APIWrapper api = (APIWrapper) wrapped.getConstructor(EmbedClass.class).newInstance(new EmbedClass());

		// Set environment and bind to a variable
		env = new LuaTable();
		api.setup(new Computer(new Config()), env);
		api.bind();

		table = api.getTable();
	}

	/**
	 * Test that functions return what the are meant to
	 */
	@Test
	public void testFunctions() {
		assertEquals(2, table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1), LuaValue.valueOf(1)).todouble(1), 0);
		assertEquals(LuaValue.NONE, table.get("noArgsNoReturn").invoke());

		assertEquals(LuaValue.TRUE, table.get("noArgsLuaReturn").invoke());

		assertEquals(2, table.get("varArgsLuaReturn").invoke(LuaValue.valueOf(2)).toint(1));
	}

	/**
	 * Test that annotations work
	 */
	@Test
	public void testAnnotation() {
		assertEquals(table.get("varArgsLuaReturn"), table.get("one"));
		assertEquals(table.get("varArgsLuaReturn"), table.get("two"));

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
	}
}
