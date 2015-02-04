package squidev.ccstudio;

import org.junit.BeforeClass;
import org.junit.Test;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class JavaBuilderTest {
	public static LuaValue globals;
	@BeforeClass
	public static void before() {
		globals = JsePlatform.debugGlobals();
		LuaJC.install();
	}

	@Test
	public void testJavaBuilder() throws Exception {
		LuaFunction func = loadString("return 'hello'");
		Varargs result = func.invoke();
		assertEquals(result.toString(), "hello");
	}

	protected InputStream makeStream(String string) {
		return new ByteArrayInputStream(string.getBytes());
	}

	protected LuaFunction loadString(String string) throws Exception {
		InputStream stream = makeStream(string);
		return LoadState.load(stream, "chunk-name", globals);
	}
}
