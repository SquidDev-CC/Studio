package squidev.ccstudio.core.apis.wrapper;

import org.luaj.vm2.LuaNumber;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.core.apis.CCAPI;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static squidev.ccstudio.core.asm.AsmUtils.*;
/**
 * Builds ASM code to call an API
 *
 * TODO: More constants, less strings
 */
public class APIBuilder {
	public static final String PARENT_NAME = Type.getInternalName(CCAPI.class);

	public static final String VARARGS = Type.getDescriptor(Varargs.class);
	public static final String INVOKE_SIGNATURE = "(" + VARARGS + "I)" + VARARGS;

	/**
	 * Map Java classes to converters
	 */
	public static final Map<Class<?>, TinyMethod> TO_LUA;

	/**
	 * Map Lua classes to converters
	 */
	public static final Map<Class<?>, TinyMethod> FROM_LUA;

	static {
		Class<?> luaValueClass = LuaValue.class;

		Map<Class<?>, TinyMethod> toLua = new HashMap<Class<?>, TinyMethod>();
		TO_LUA = toLua;

		toLua.put(boolean.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", boolean.class));
		toLua.put(int.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", int.class));
		toLua.put(double.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", double.class));
		toLua.put(String.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", String.class));

		Map<Class<?>, TinyMethod> fromLua = new HashMap<Class<?>, TinyMethod>();
		FROM_LUA = fromLua;

		fromLua.put(boolean.class, TinyMethod.tryConstruct(luaValueClass, "toboolean"));
		fromLua.put(byte.class, TinyMethod.tryConstruct(luaValueClass, "tobyte"));
		fromLua.put(char.class, TinyMethod.tryConstruct(luaValueClass, "tochar"));
		fromLua.put(double.class, TinyMethod.tryConstruct(luaValueClass, "todouble"));
		fromLua.put(float.class, TinyMethod.tryConstruct(luaValueClass, "tofloat"));
		fromLua.put(int.class, TinyMethod.tryConstruct(luaValueClass, "toint"));
		fromLua.put(long.class, TinyMethod.tryConstruct(luaValueClass, "tolong"));
		fromLua.put(short.class, TinyMethod.tryConstruct(luaValueClass, "toshort"));
		fromLua.put(String.class, TinyMethod.tryConstruct(luaValueClass, "tojstring"));
	}

	public static byte[] createAPI(Class<?> reflection) {
		String originalName = Type.getInternalName(reflection);
		String className = originalName + APIClassLoader.SUFFIX;

		// Stores the 'whole name' for a variable
		String originalWhole = "L" + originalName + ";";

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		MethodVisitor mv;

		// Declare class name
		cw.visit(
				V1_6, ACC_PUBLIC + ACC_SUPER, className, // Public, and
				"L" + PARENT_NAME + "<" + originalWhole + ">;", // Generic
				PARENT_NAME, null
		);

		// Declare METHOD_NAMES
		cw.visitField(ACC_PROTECTED + ACC_FINAL + ACC_STATIC, "METHOD_NAMES", "[Ljava/lang/String;", null, null);

		// Read functions and build a list of them
		List<LuaMethod> methods = new ArrayList<LuaMethod>();
		for(Method m : reflection.getMethods()) {
			if (m.isAnnotationPresent(LuaFunction.class)) {
				// Append items to the list
				methods.add(new LuaMethod(m));
			}

		}

		// Handle Static constructor (Loads method names)
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();

			constantOpcode(mv, methods.size());
			mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

			int counter = 0;
			for(LuaMethod m : methods) {
				mv.visitInsn(DUP);

				constantOpcode(mv, counter);

				mv.visitLdcInsn(m.getLuaName());
				mv.visitInsn(AASTORE);

				++counter;
			}

			mv.visitFieldInsn(PUTSTATIC, className, "METHOD_NAMES", "[Ljava/lang/String;");
			mv.visitInsn(RETURN);

			mv.visitMaxs(4, 0); // Always the same
			mv.visitEnd();
		}

		// Handle constructor
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + originalWhole + ")V", null, null);
			mv.visitCode();

			// Parent constructor with argument
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, PARENT_NAME, "<init>", "(Ljava/lang/Object;)V", false);

			// Set method names
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETSTATIC, className, "METHOD_NAMES", "[Ljava/lang/String;");
			mv.visitFieldInsn(PUTFIELD, className, "methodNames", "[Ljava/lang/String;");

			// And return
			mv.visitInsn(RETURN);

			mv.visitMaxs(2, 2); // Always the same
			mv.visitEnd();
		}

		{ // Handle invoking
			mv = cw.visitMethod(ACC_PUBLIC, "invoke", INVOKE_SIGNATURE, null, null);
			mv.visitCode();

			// Get index
			mv.visitVarInsn(ILOAD, 2);

			Label defaultLabel = new Label();

			int size = methods.size();
			int[] numbers = new int[size];
			Label[] labels = new Label[size];

			for(int i = 0; i < size; i++) {
				numbers[i] = i;
				labels[i] = new Label();
			}

			// Create a switch
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
				if(arguments.length == 1 && arguments[0].equals(Varargs.class)) {
					// No validation required
				} else if(arguments.length != 0) {
					StringBuilder builder = new StringBuilder("Expected ");

					int length = arguments.length;
					Label doException = new Label();
					Label noException = new Label();

					// Check has enough arguments
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "narg", "()I", false);
					constantOpcode(mv, length);
					mv.visitJumpInsn(IF_ICMPLT, doException);

					// TODO: Handle this properly (Will break if the LuaValue is the last option)
					int argCounter = 0;
					for(Class<?> arg : arguments) {
						String name;
						boolean isLast = length == (argCounter + 1);

						// arg.get(<argCounter>)
						mv.visitVarInsn(ALOAD, 1);
						constantOpcode(mv, argCounter + 1);
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);

						// TODO: Make this nicer
						if(arg.equals(boolean.class)) {
							name = "boolean";
							mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isboolean", "()Z", false);
						} else if(arg.equals(byte.class) || arg.equals(int.class) || arg.equals(char.class) || arg.equals(short.class)) {
							name = "number";
							mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isint", "()Z", false);
						} else if(arg.equals(float.class) || arg.equals(double.class)) {
							name = "number";
							mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isnumber", "()Z", false);
						} else if(arg.equals(long.class)) {
							name = "number";
							mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "islong", "()Z", false);
						} else if(arg.equals(String.class)) {
							name = "string";
							mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isstring", "()Z", false);
						} else if(arg.equals(LuaValue.class)) {
							// TODO: Make LuaValue handling better (support LuaNumber - for example)
							name = arg.getSimpleName();
						} else {
							throw new RuntimeException("Cannot validate " + arg.getName());
						}

						if(isLast) {
							// If (condition) is false then skip to noException, else doException
							mv.visitJumpInsn(IFNE, noException);
						} else {
							// If (condition) is true then doException
							mv.visitJumpInsn(IFEQ, doException);
						}

						builder.append(name);
						if(!isLast) {
							builder.append(", ");
						}

						++argCounter;
					}

					// Do exception
					mv.visitLabel(doException);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitTypeInsn(NEW, "org/luaj/vm2/LuaError");
					mv.visitInsn(DUP);
					mv.visitLdcInsn(builder.toString());
					mv.visitMethodInsn(INVOKESPECIAL, "org/luaj/vm2/LuaError", "<init>", "(Ljava/lang/String;)V", false);
					mv.visitInsn(ATHROW);

					// Continue
					mv.visitLabel(noException);
					mv.visitFrame(F_SAME, 0, null, 0, null);
				}

				// Check the object is correct
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, "instance", "Ljava/lang/Object;");
				mv.visitTypeInsn(CHECKCAST, originalName);

				// Load the arguments
				if(arguments.length == 1 && arguments[0].equals(Varargs.class)) {
					// Load the VarArgs as an argument (no validation required)
					mv.visitVarInsn(ALOAD, 1);
				} else {
					int argCounter = 1;
					for(Class<?> arg : arguments) {
						mv.visitVarInsn(ALOAD, 1);
						constantOpcode(mv, argCounter);
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);

						// If we don't need to convert
						if (!arg.equals(LuaValue.class)) {
							// Check if we have a converter
							TinyMethod type = FROM_LUA.get(arg);
							if(type == null) throw new RuntimeException("Cannot convert LuaValue to " + arg.getName());

							type.inject(mv, INVOKEVIRTUAL);
						}

						argCounter += 1;
					}
				}

				// And call it
				mv.visitMethodInsn(INVOKEVIRTUAL, originalName, method.getJavaName(), Type.getMethodDescriptor(method.method), false);


				if(returns.equals(Void.TYPE)) {
					// If no result, return None
					mv.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
				} else if(!Varargs.class.isAssignableFrom(returns)) { // Don't need to convert if returning a LuaValue
					// Check if we have a converter
					TinyMethod type = TO_LUA.get(returns);
					if(type == null) throw new RuntimeException("Cannot convert " + returns.getName() + " to LuaValue");

					type.inject(mv, INVOKESTATIC);
				}

				mv.visitInsn(ARETURN);
				counter++;
			}

			// default:
			mv.visitLabel(defaultLabel);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			// return LuaValue.NONE;
			mv.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
			mv.visitInsn(ARETURN);

			mv.visitMaxs(0, 0);

			mv.visitEnd();
		}

		cw.visitEnd();

		// TODO: Remove this
		CheckClassAdapter.verify(new ClassReader(cw.toByteArray()), false, new PrintWriter(System.out));

		return cw.toByteArray();
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

		/**
		 * Get the name of the Java class
		 * @return
		 */
		public String getJavaName() {
			return method.getName();
		}
	}
}
