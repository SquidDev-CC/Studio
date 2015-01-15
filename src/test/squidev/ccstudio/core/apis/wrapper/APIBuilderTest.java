package squidev.ccstudio.core.apis.wrapper;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.core.apis.CCAPI;
import squidev.ccstudio.core.testutils.ExpectException;

import static org.junit.Assert.*;

public class APIBuilderTest {
	private static LuaTable table;

	@BeforeClass
	public static void testCreateAPI() throws Exception {
		APIClassLoader loader = new APIClassLoader();
		Class<?> wrapped = loader.findClass(EmbedClass.class);

		CCAPI api = (CCAPI) wrapped.getConstructor(EmbedClass.class).newInstance(new EmbedClass());
		table = api.getTable();
	}

	@Test
	public void testFunctions() {
		assertEquals(2, table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1), LuaValue.valueOf(1)).todouble(1), 0);
		assertEquals(LuaValue.NONE, table.get("noArgsNoReturn").invoke());

		assertEquals(LuaValue.TRUE, table.get("noArgsLuaReturn").invoke());

		assertEquals(2, table.get("varArgsLuaReturn").invoke(LuaValue.valueOf(2)).toint(1));
	}

	@Test
	public void testErrors() {
		ExpectException.expect(LuaError.class, "Expected number, number",
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(true), LuaValue.valueOf(1)),
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf("HELLO"), LuaValue.valueOf(1)),
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1), LuaValue.valueOf(1.12)),
			() -> table.get("twoArgsOneReturn").invoke(LuaValue.valueOf(1))
		);
	}

	public static class EmbedClass {
		@LuaFunction
		public void noArgsNoReturn() { }

		@LuaFunction
		public double twoArgsOneReturn(double a, int b) { return a + b; }

		@LuaFunction
		public LuaValue noArgsLuaReturn() { return LuaValue.TRUE; }

		@LuaFunction
		public Varargs varArgsLuaReturn(Varargs args) { return args; }
	}
}
