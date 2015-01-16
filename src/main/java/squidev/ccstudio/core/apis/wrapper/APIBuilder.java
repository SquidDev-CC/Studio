package squidev.ccstudio.core.apis.wrapper;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import squidev.ccstudio.core.Config;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static squidev.ccstudio.core.asm.AsmUtils.TinyMethod;
import static squidev.ccstudio.core.asm.AsmUtils.constantOpcode;

/**
 * Builds ASM code to call an API
 *
 * TODO: More constants, less strings
 */
public class APIBuilder {
	public static final String PARENT_NAME = Type.getInternalName(APIWrapper.class);

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

		Map<Class<?>, TinyMethod> toLua = new HashMap<>();
		TO_LUA = toLua;

		toLua.put(boolean.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", boolean.class));
		toLua.put(int.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", int.class));
		toLua.put(double.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", double.class));
		toLua.put(String.class, TinyMethod.tryConstruct(luaValueClass, "valueOf", String.class));

		Map<Class<?>, TinyMethod> fromLua = new HashMap<>();
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

	final Class<?> reflection;
	final ClassWriter writer;

	final String originalName;
	final String className;

	/**
	 * The whole name of the original name ('L' + ... + ';')
	 */
	final String originalWhole;

	/**
	 * Names that this should be set to
	 */
	String[] names = null;

	/**
	 * List of methods
	 */
	List<LuaMethod> methods;

	public APIBuilder(Class<?> reflection) {
		this.reflection = reflection;

		originalName = Type.getInternalName(reflection);
		className = originalName + APIClassLoader.SUFFIX;
		originalWhole = Type.getDescriptor(reflection);

		writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		write();
	}

	/**
	 * Write everything!
	 */
	void write() {
		// Declare class name
		writer.visit(
				V1_6, ACC_PUBLIC + ACC_SUPER, className, // Public, and
				"L" + PARENT_NAME + "<" + originalWhole + ">;", // Generic
				PARENT_NAME, null
		);

		// Declare METHOD_NAMES
		writer.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "METHOD_NAMES", "[[Ljava/lang/String;", null, null);

		// Declare NAMES
		writer.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "NAMES", "[Ljava/lang/String;", null, null);

		// Read all methods
		methods = new ArrayList<>();
		for(Method m : reflection.getMethods()) {
			if (m.isAnnotationPresent(LuaFunction.class)) {
				// Append items to the list
				methods.add(new LuaMethod(m));
			}
		}

		if (reflection.isAnnotationPresent(LuaAPI.class)) {
			names = reflection.getAnnotation(LuaAPI.class).value();
			// If we have the LuaAPI annotation then
			// we should ensure that this is set as an API
			if (names == null || names.length == 0) {
				names = new String[]{reflection.getSimpleName().toLowerCase()};
			}
		}

		writeInit();
		writeStaticInit();
		writeInvoke();

		writer.visitEnd();
	}

	/**
	 * Write the static constructor
	 * This constructs the array of array of names.
	 */
	void writeStaticInit() {
		MethodVisitor mv = writer.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitCode();

		constantOpcode(mv, methods.size());
		mv.visitTypeInsn(ANEWARRAY, "[Ljava/lang/String;");

		int counter = 0;
		for (LuaMethod m : methods) {
			// For key <counter>
			mv.visitInsn(DUP);
			constantOpcode(mv, counter);

			String[] names = m.getLuaName();

			// Create an array of length <names.length>
			constantOpcode(mv, names.length);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

			int nameCounter = 0;
			for (String name : names) {
				mv.visitInsn(DUP);
				constantOpcode(mv, nameCounter);
				mv.visitLdcInsn(name);
				mv.visitInsn(AASTORE);

				++nameCounter;
			}

			// And store
			mv.visitInsn(AASTORE);

			++counter;
		}

		mv.visitFieldInsn(PUTSTATIC, className, "METHOD_NAMES", "[[Ljava/lang/String;");

		// Visit names
		if (names == null) {
			mv.visitInsn(ACONST_NULL);
		} else {
			constantOpcode(mv, names.length);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

			counter = 0;
			for (String name : names) {
				mv.visitInsn(DUP);
				constantOpcode(mv, counter);
				mv.visitLdcInsn(name);
				mv.visitInsn(AASTORE);

				++counter;
			}
		}

		mv.visitFieldInsn(PUTSTATIC, className, "NAMES", "[Ljava/lang/String;");

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	/**
	 * Write the constructor. This calls the parent constructor,
	 * sets the instance and sets the method names to be the static field
	 */
	void writeInit() {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "(" + originalWhole + ")V", null, null);
		mv.visitCode();

		// Parent constructor with argument
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, PARENT_NAME, "<init>", "(Ljava/lang/Object;)V", false);

		// Set method names
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETSTATIC, className, "METHOD_NAMES", "[[Ljava/lang/String;");
		mv.visitFieldInsn(PUTFIELD, className, "methodNames", "[[Ljava/lang/String;");

		// Set method API names
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETSTATIC, className, "NAMES", "[Ljava/lang/String;");
		mv.visitFieldInsn(PUTFIELD, className, "names", "[Ljava/lang/String;");

		// And return
		mv.visitInsn(RETURN);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	void writeInvoke() {
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "invoke", INVOKE_SIGNATURE, null, null);
		mv.visitCode();

		// Get index
		mv.visitVarInsn(ILOAD, 2);

		Label defaultLabel = new Label();

		int size = methods.size();
		Label[] labels = new Label[size];

		for (int i = 0; i < size; i++) {
			labels[i] = new Label();
		}

		// Create a switch
		mv.visitTableSwitchInsn(0, size - 1, defaultLabel, labels);

		int counter = 0;
		for (LuaMethod method : methods) {
			// Initial stuff
			mv.visitLabel(labels[counter]);
			mv.visitFrame(F_SAME, 0, null, 0, null);

			// Load instance
			Class<?>[] arguments = method.method.getParameterTypes();
			Class<?> returns = method.method.getReturnType();

			// Avoid argument validation
			if (arguments.length == 1 && arguments[0].equals(Varargs.class)) {
				/* No validation required */
			} else if (arguments.length != 0) {
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
				for (Class<?> arg : arguments) {
					String name;
					boolean isLast = length == (argCounter + 1);

					// arg.get(<argCounter>)
					mv.visitVarInsn(ALOAD, 1);
					constantOpcode(mv, argCounter + 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);

					// TODO: Make this nicer
					if (arg.equals(boolean.class)) {
						name = "boolean";
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isboolean", "()Z", false);
					} else if (arg.equals(byte.class) || arg.equals(int.class) || arg.equals(char.class) || arg.equals(short.class)) {
						name = "number";
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isint", "()Z", false);
					} else if (arg.equals(float.class) || arg.equals(double.class)) {
						name = "number";
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isnumber", "()Z", false);
					} else if (arg.equals(long.class)) {
						name = "number";
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "islong", "()Z", false);
					} else if (arg.equals(String.class)) {
						name = "string";
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isstring", "()Z", false);
					} else if (arg.equals(LuaValue.class)) {
						// TODO: Make LuaValue handling better (support LuaNumber - for example)
						name = arg.getSimpleName();
					} else {
						throw new RuntimeException("Cannot validate " + arg.getName());
					}

					if (isLast) {
						// If (condition) is false then skip to noException, else doException
						mv.visitJumpInsn(IFNE, noException);
					} else {
						// If (condition) is true then doException
						mv.visitJumpInsn(IFEQ, doException);
					}

					builder.append(name);
					if (!isLast) {
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
			if (arguments.length == 1 && arguments[0].equals(Varargs.class)) {
				// Load the VarArgs as an argument (no validation required)
				mv.visitVarInsn(ALOAD, 1);
			} else {
				int argCounter = 1;
				for (Class<?> arg : arguments) {
					mv.visitVarInsn(ALOAD, 1);
					constantOpcode(mv, argCounter);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);

					// If we don't need to convert
					if (!arg.equals(LuaValue.class)) {
						// Check if we have a converter
						TinyMethod type = FROM_LUA.get(arg);
						if (type == null) throw new RuntimeException("Cannot convert LuaValue to " + arg.getName());

						type.inject(mv, INVOKEVIRTUAL);
					}

					argCounter += 1;
				}
			}

			// And call it
			mv.visitMethodInsn(INVOKEVIRTUAL, originalName, method.getJavaName(), Type.getMethodDescriptor(method.method), false);


			if (returns.equals(Void.TYPE)) {
				// If no result, return None
				mv.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
			} else if (!Varargs.class.isAssignableFrom(returns)) { // Don't need to convert if returning a LuaValue
				// Check if we have a converter
				TinyMethod type = TO_LUA.get(returns);
				if (type == null) throw new RuntimeException("Cannot convert " + returns.getName() + " to LuaValue");

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

	public byte[] toByteArray() {
		byte[] bytes = writer.toByteArray();
		if (Config.verifySources) CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.out));

		return bytes;
	}

	/**
	 * Stores all data associated with a Lua function
	 */
	public static class LuaMethod {
		public final LuaFunction function;
		public final Method method;

		public LuaMethod(Method m) {
			this(m.getAnnotation(LuaFunction.class), m);
		}

		public LuaMethod(LuaFunction f, Method m) {
			function = f;
			method = m;
		}

		/**
		 * Get the names of the Lua function
		 * @return A list of method names
		 */
		public String[] getLuaName() {
			String[] luaName = function.value();
			if (luaName == null || luaName.length == 0 || (luaName.length == 1 && luaName[0].isEmpty())) {
				return new String[]{method.getName()};
			}
			return luaName;
		}

		/**
		 * Get the name of the Java method
		 * @return The Java method name
		 */
		public String getJavaName() {
			return method.getName();
		}
	}
}
