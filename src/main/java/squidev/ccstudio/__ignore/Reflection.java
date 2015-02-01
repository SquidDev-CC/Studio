package squidev.ccstudio.__ignore;

import org.luaj.vm2.LuaValue;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public class Reflection {

	public static void main(String[] args) {
		System.out.println(LuaValue.class.toString());
		System.out.println(LuaValue.class.getName());
		System.out.println(LuaValue.class.getCanonicalName());
		System.out.println(LuaValue.class.getSimpleName());

		for (Method m : LuaValue.class.getMethods()) {
			System.out.println("    " + m.toString() + "    " + Type.getMethodDescriptor(m));
		}

		System.out.println(Type.getDescriptor(LuaValue.class) + " " + Type.getInternalName(LuaValue.class));
	}
}
