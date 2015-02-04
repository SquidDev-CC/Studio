package squidev.ccstudio;

import org.junit.BeforeClass;
import org.junit.Test;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJCWorking;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class JavaBuilderTest {
	public static LuaValue globals;
	@BeforeClass
	public static void before() {
		globals = JsePlatform.debugGlobals();
		LuaJCWorking.install();
	}

	@Test
	public void testJavaBuilder() throws Exception {
		LuaFunction func = loadString("local j = '' for i = 0, 9, 1 do j = j .. i end if j ~= '0123456789' then return 'Um ' .. j end return 'hello'");
		Varargs result = func.invoke();
		assertEquals("hello", result.toString());
	}

	protected InputStream makeStream(String string) {
		return new ByteArrayInputStream(string.getBytes());
	}

	protected LuaFunction loadString(String string) throws Exception {
		InputStream stream = makeStream(string);
		return LoadState.load(stream, "chunk-name", globals);
	}
}
