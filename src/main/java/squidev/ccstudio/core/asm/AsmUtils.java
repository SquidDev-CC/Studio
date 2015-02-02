package squidev.ccstudio.core.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

/**
 * Utilities for writing asm
 */
public class AsmUtils {
	/**
	 * Get the appropriate constant opcode
	 *
	 * @param number The opcode number
	 * @return ICONST_n or -1 if doesn't exist
	 */
	public static int getConstOpcode(int number) {
		switch (number) {
			case 0:
				return ICONST_0;
			case 1:
				return ICONST_1;
			case 2:
				return ICONST_2;
			case 3:
				return ICONST_3;
			case 4:
				return ICONST_4;
			case 5:
				return ICONST_5;
		}

		return -1;
	}

	/**
	 * Insert the correct Opcode for Java constants
	 *
	 * @param mv     The {@see MethodVisitor}
	 * @param number The constant to insert
	 */
	public static void constantOpcode(MethodVisitor mv, int number) {
		if (number >= 0 && number <= 5) {
			mv.visitInsn(getConstOpcode(number));
		} else if(number >= -128 && number <= 127) {
			mv.visitIntInsn(BIPUSH, number);
		} else if(number >= -32768 && number <= 32767) {
			mv.visitIntInsn(SIPUSH, (short)number);
		} else {
			mv.visitLdcInsn(number);
		}
	}

	/**
	 * Get the method signature
	 *
	 * @param classObj   The class to find it from
	 * @param methodName The method name
	 * @param args       Argument types
	 * @return The method signature or {@code null} or failure
	 */
	public static String getMethodDecriptor(Class<?> classObj, String methodName, Class<?>... args) {
		try {
			return Type.getMethodDescriptor(classObj.getMethod(methodName, args));
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Stores very basic data about a method so we can inject it
	 */
	public static class TinyMethod {
		public final String className;
		public final String name;
		public final String signature;

		public final Boolean isStatic;

		public TinyMethod(String className, String name, String signature, boolean isStatic) {
			this.className = className;
			this.name = name;
			this.signature = signature;
			this.isStatic = isStatic;
		}

		public TinyMethod(String className, String name, String signature) {
			this(className, name, signature, false);
		}

		public TinyMethod(Method m) {
			this(Type.getInternalName(m.getDeclaringClass()), m.getName(), Type.getMethodDescriptor(m), Modifier.isStatic(m.getModifiers()));
		}

		public static TinyMethod tryConstruct(Class<?> classObj, String methodName, Class<?>... args) {
			try {
				return new TinyMethod(classObj.getMethod(methodName, args));
			} catch (NoSuchMethodException e) {
				return null;
			}
		}

		public void inject(MethodVisitor mv, int opcode) {
			mv.visitMethodInsn(opcode, className, name, signature, false);
		}

		public void inject(MethodVisitor mv) {
			inject(mv, isStatic ? INVOKESTATIC : INVOKEVIRTUAL);
		}
	}
}
