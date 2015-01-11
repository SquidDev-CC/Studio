package squidev.ccstudio.core.apis.wrapper;

import org.luaj.vm2.Varargs;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import squidev.ccstudio.core.apis.CCAPI;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
/**
 * squidev.ccstudio.computer.apis (CCStudio.Java
 */
public class APIBuilder {
	public static final String PARENT_NAME = CCAPI.class.getName().replace('.', '/');

	public static final String VARARGS = "L" + Varargs.class.getName().replace('.', '/') + ";";
	public static final String INVOKE_SIGNATURE = "(" + VARARGS + ")" + VARARGS;

	public static byte[] createAPI(Class<?> reflection) {
		String originalName = reflection.getName().replace('.', '/');
		String className = originalName + APIClassLoader.SUFFIX;

		// Stores the 'whole name' for a variable
		String originalWhole = "L" + originalName + ";";

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		MethodVisitor mv;

		// Declare class name
		cw.visit(
				V1_6, ACC_PUBLIC + ACC_SUPER, className, // Public, and
				"L" + PARENT_NAME + "<" + originalWhole + ">;", // Generic
				PARENT_NAME, null
		);

		// Declare METHOD_NAMES
		cw.visitField(ACC_PROTECTED + ACC_FINAL + ACC_STATIC, "METHOD_NAMES", "[Ljava/lang/String;", null, null);

		// Store Lua methods
		List<LuaMethod> methods = new ArrayList<LuaMethod>();

		// Handle Static constructor (Loads method names)
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();

			int counter = 0;

			mv.visitIntInsn(BIPUSH, methods.size());
			mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

			// Read functions and build a list of them
			for(Method m : reflection.getMethods()) {
				if (m.isAnnotationPresent(LuaFunction.class)) {
					LuaMethod method = new LuaMethod(m);

					// Append items to the list
					methods.add(method);

					mv.visitInsn(DUP);

					if(counter < 6) {
						mv.visitInsn(getConstOpcode(counter));
					} else {
						mv.visitIntInsn(BIPUSH, counter);
					}

					mv.visitLdcInsn(method.getLuaName());
					mv.visitInsn(AASTORE);

					++counter;
				}

			}

			mv.visitFieldInsn(PUTSTATIC, className, "METHOD_NAMES", "[Ljava/lang/String;");
			mv.visitInsn(RETURN);
		}

		// Handle constructor
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + originalWhole + ")V", null, null);
			mv.visitCode();

			// Parent constructor with argument
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, PARENT_NAME, "<init>", "(Ljava/lang/Object;)V");

			// Set method names
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETSTATIC, className, "METHOD_NAMES", "[Ljava/lang/String;");
			mv.visitFieldInsn(PUTFIELD, className, "methodNames", "[Ljava/lang/String;");

			// And return
			mv.visitInsn(RETURN);

			mv.visitEnd();
		}

		{
			mv = cw.visitMethod(ACC_PUBLIC, "invoke", INVOKE_SIGNATURE, null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "opcode", "I");

			Label defaultLabel = new Label();

			int size = methods.size();
			int[] numbers = new int[size];
			Label[] labels = new Label[size];

			for(int i = 0; i < size; i++) {
				numbers[i] = i;
				labels[i] = new Label();
			}

			mv.visitLookupSwitchInsn(defaultLabel, numbers, labels);

			int counter = 0;
			for(LuaMethod method : methods) {
				// Initial stuff
				mv.visitLabel(labels[counter]);
				mv.visitFrame(F_SAME, 0, null, 0, null);

				// Load instance
				Class<?>[] arguments = method.method.getParameterTypes();
				Class<?> returns = method.method.getReturnType();

				// Avoid argument validation
				if(arguments.length == 0) {

				}

				// Avoid result validation
				if(returns.equals(Void.TYPE)) {

				}


				counter++;
			}
		}





		return cw.toByteArray();
	}

	/**
	 * Get the appropriate constant opcode
	 * @param number The opcode number
	 * @return ICONST_n or 0 if doesn't exist
	 */
	public static int getConstOpcode(int number) {
		switch(number) {
			case 0: return ICONST_0;
			case 1: return ICONST_1;
			case 2: return ICONST_2;
			case 3: return ICONST_3;
			case 4: return ICONST_4;
			case 5: return ICONST_5;
		}

		return 0;
	}

	/**
	 * Stores all data associated with a Lua function
	 */
	public static class LuaMethod {
		public LuaFunction function;
		public Method method;

		protected String name;

		public LuaMethod(Method m) {
			this(m.getAnnotation(LuaFunction.class), m);
		}

		public LuaMethod(LuaFunction f, Method m) {
			function = f;
			method = m;
		}

		/**
		 * Get the name of the Lua function
		 * @return
		 */
		public String getLuaName() {
			String luaName = function.name();
			if(luaName == null || luaName.isEmpty()) {
				return method.getName();
			}
			return luaName;
		}
	}
}
