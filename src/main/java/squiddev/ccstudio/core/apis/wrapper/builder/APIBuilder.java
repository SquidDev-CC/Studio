package squiddev.ccstudio.core.apis.wrapper.builder;

import org.luaj.vm2.*;
import org.objectweb.asm.*;
import squiddev.ccstudio.core.Config;
import squiddev.ccstudio.core.apis.wrapper.ILuaValidator;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;
import squiddev.ccstudio.core.asm.AsmUtils;
import squiddev.ccstudio.core.luaj.Conversion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static squiddev.ccstudio.core.asm.AsmUtils.TinyMethod;
import static squiddev.ccstudio.core.asm.AsmUtils.constantOpcode;

/**
 * Builds ASM code to call an API
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
		Map<Class<?>, TinyMethod> toLua = new HashMap<>();
		TO_LUA = toLua;

		// Boolean
		toLua.put(boolean.class, TinyMethod.tryConstruct(LuaBoolean.class, "valueOf", boolean.class));
		toLua.put(boolean[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", boolean[].class));

		// Integers
		toLua.put(int.class, TinyMethod.tryConstruct(LuaInteger.class, "valueOf", int.class));
		toLua.put(int[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", int[].class));

		toLua.put(byte.class, TinyMethod.tryConstruct(LuaInteger.class, "valueOf", int.class));
		toLua.put(byte[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", byte[].class));

		toLua.put(short.class, TinyMethod.tryConstruct(LuaInteger.class, "valueOf", int.class));
		toLua.put(short[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", short[].class));

		toLua.put(char.class, TinyMethod.tryConstruct(LuaInteger.class, "valueOf", int.class));
		toLua.put(char[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", char[].class));

		toLua.put(long.class, TinyMethod.tryConstruct(LuaInteger.class, "valueOf", long.class));
		toLua.put(long[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", long[].class));

		// Floats
		toLua.put(double.class, TinyMethod.tryConstruct(LuaDouble.class, "valueOf", double.class));
		toLua.put(double[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", double[].class));

		toLua.put(float.class, TinyMethod.tryConstruct(LuaDouble.class, "valueOf", double.class));
		toLua.put(float[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", float[].class));

		// String
		toLua.put(String.class, TinyMethod.tryConstruct(Conversion.class, "valueOf", String.class));
		toLua.put(String[].class, TinyMethod.tryConstruct(Conversion.class, "valueOf", String[].class));

		Map<Class<?>, TinyMethod> fromLua = new HashMap<>();
		FROM_LUA = fromLua;

		fromLua.put(boolean.class, TinyMethod.tryConstruct(LuaValue.class, "toboolean"));
		fromLua.put(byte.class, TinyMethod.tryConstruct(LuaValue.class, "tobyte"));
		fromLua.put(char.class, TinyMethod.tryConstruct(LuaValue.class, "tochar"));
		fromLua.put(double.class, TinyMethod.tryConstruct(LuaValue.class, "todouble"));
		fromLua.put(float.class, TinyMethod.tryConstruct(LuaValue.class, "tofloat"));
		fromLua.put(int.class, TinyMethod.tryConstruct(LuaValue.class, "toint"));
		fromLua.put(long.class, TinyMethod.tryConstruct(LuaValue.class, "tolong"));
		fromLua.put(short.class, TinyMethod.tryConstruct(LuaValue.class, "toshort"));
		fromLua.put(String.class, TinyMethod.tryConstruct(LuaValue.class, "tojstring"));
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

		writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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
		writer.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "METHOD_NAMES", "[[Ljava/lang/String;", null, null);

		// Declare NAMES
		writer.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "NAMES", "[Ljava/lang/String;", null, null);

		// Read all methods
		methods = new ArrayList<>();
		for (Method m : reflection.getMethods()) {
			if (m.isAnnotationPresent(LuaFunction.class)) {
				// Append items to the list
				methods.add(new LuaMethod(m));
			}
		}

		if(methods.size() == 0) throw new BuilderException("No LuaFunction methods", reflection);

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

			LuaMethod.ValidationIterator iterator = method.validationIterator();
			StringBuilder builder = new StringBuilder("Expected ");
			boolean needsValidation = iterator.hasValidateNext();

			Label doException = new Label();
			Label noException = new Label();

			if (needsValidation) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "narg", "()I", false);
				constantOpcode(mv, iterator.length());
				mv.visitJumpInsn(IF_ICMPLT, doException);
			}

			int index = 1;
			while (iterator.hasNext()) {
				LuaMethod.LuaArgument arg = iterator.next();
				Class<?> type = arg.type;
				ILuaValidator validator = arg.getValidator();

				// If the item is a varargs then we shouldn't give it a name
				// Varargs will always be the last item
				if (!type.equals(Varargs.class)) builder.append(validator.getName(type)).append(", ");

				if (validator.shouldValidate(type)) {
					mv.visitVarInsn(ALOAD, 1);
					constantOpcode(mv, index);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);
					validator.addValidation(mv, type);

					if (iterator.hasValidateNext()) {
						// If (condition) is false (== 0) then go to exception, else continue
						mv.visitJumpInsn(IFEQ, doException);
					} else {
						// If (condition) is true (== 1) then no exception
						mv.visitJumpInsn(IFNE, noException);
					}
				}

				++index;
			}

			if (needsValidation) {
				// Do exception
				mv.visitLabel(doException);
				mv.visitFrame(F_SAME, 0, null, 0, null);
				mv.visitTypeInsn(NEW, "org/luaj/vm2/LuaError");
				mv.visitInsn(DUP);

				String error = method.getError();
				String text = builder.toString();
				if (error == null) {
					if (text.endsWith(", ")) text = text.substring(0, text.length() - 2);
					error = text;
				}
				mv.visitLdcInsn(error);
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
			int argCounter = 1;
			iterator.rewind();
			for (LuaMethod.LuaArgument arg : iterator) {
				mv.visitVarInsn(ALOAD, 1);

				Class<?> argType = arg.type;
				if (argType.equals(Varargs.class)) {
					// If we just have varargs then we should load it, if we have varargs later then use subargs
					if (iterator.length() > 1) {
						constantOpcode(mv, argCounter);
						mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "subargs", "(I)Lorg/luaj/vm2/Varargs;", false);
					}
				} else {
					constantOpcode(mv, argCounter);
					mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);

					if (LuaValue.class.isAssignableFrom(argType)) {
						// Cast to the type required
						mv.visitTypeInsn(CHECKCAST, Type.getInternalName(argType));
					} else {
						TinyMethod type = FROM_LUA.get(argType);
						if (type == null) {
							throw new BuilderException("Cannot convert LuaValue to " + argType, method);
						}

						type.inject(mv, INVOKEVIRTUAL);
					}
				}

				++argCounter;
			}

			// And call it
			mv.visitMethodInsn(INVOKEVIRTUAL, originalName, method.getJavaName(), Type.getMethodDescriptor(method.method), false);

			Class<?> returns = method.method.getReturnType();
			if (returns.equals(Void.TYPE)) {
				// If no result, return None
				mv.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
			} else if (!Varargs.class.isAssignableFrom(returns)) { // Don't need to convert if returning a LuaValue

				if (!returns.isArray() || !LuaValue.class.isAssignableFrom(returns.getComponentType())) {
					// Check if we have a converter
					TinyMethod type = TO_LUA.get(returns);
					if (type == null) {
						throw new BuilderException("Cannot convert " + returns.getName() + " to LuaValue for ", method);
					}

					type.inject(mv, INVOKESTATIC);
				}

				// If we return an array then try return a {@link LuaTable} or {@link Varargs}
				if (returns.isArray()) {
					if (method.function.isVarArgs()) {
						mv.visitMethodInsn(INVOKESTATIC, "org/luaj/vm2/LuaValue", "varargsOf", "([Lorg/luaj/vm2/LuaValue;)Lorg/luaj/vm2/Varargs;", false);
					} else {
						mv.visitMethodInsn(INVOKESTATIC, "org/luaj/vm2/LuaValue", "listOf", "([Lorg/luaj/vm2/LuaValue;)Lorg/luaj/vm2/LuaTable;", false);
					}
				}
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
		if (Config.verifySources) {
			AsmUtils.validateClass(bytes);
		}

		return bytes;
	}

	public static class BuilderException extends RuntimeException {
		public BuilderException(String message) {
			super(message);
		}

		public BuilderException(String message, Class javaClass) {
			this(javaClass.getName() + ": " + message);
		}

		public BuilderException(String message, Method method) {
			this(method.getDeclaringClass().getName() + ":" + method.getName() + ": " + message);
		}

		public BuilderException(String message, LuaMethod method) {
			this(message, method.method);
		}
	}
}
