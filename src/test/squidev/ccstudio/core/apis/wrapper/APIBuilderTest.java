package squidev.ccstudio.core.apis.wrapper;

import org.junit.Test;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.core.apis.CCAPI;

public class APIBuilderTest {

	@Test
	public void testCreateAPI() throws Exception {
		APIClassLoader loader = new APIClassLoader();
		Class<?> wrapped = loader.findClass(EmbedClass.class);

		CCAPI api = (CCAPI)wrapped.getConstructor(EmbedClass.class).newInstance(new EmbedClass());
		LuaTable table = api.getTable();
		table.get("hello").invoke();
	}

	public static class EmbedClass {
		@LuaFunction
		public void noArgsNoReturn() { }

		@LuaFunction
		public double twoArgsOneReturn(double a, double b) { return 0; }

		@LuaFunction
		public LuaValue noArgsLuaReturn() { return LuaValue.NONE; }

		@LuaFunction
		public LuaValue varArgsLuaReturn(Varargs args) { return LuaValue.NONE; }
	}
}
