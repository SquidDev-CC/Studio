package squiddev.ccstudio.core.apis.wrapper;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import squiddev.ccstudio.core.apis.wrapper.builder.APIBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Validates values using {@see LuaValue.isnumber()} methods or <code>instanceof</code> if an exact type is specified
 */
public class DefaultLuaValidator implements ILuaValidator {
	public static final Map<Class<?>, String> CLASS_NAMES;

	/**
	 * Should validation occur on this argument
	 *
	 * @param type The type of the argument to validate
	 * @return Should the argument be validated?
	 */
	@Override
	public boolean shouldValidate(Class<?> type) {
		if (type.equals(LuaValue.class) || type.equals(Varargs.class)) return false;

		if (
			type.equals(boolean.class) || type.equals(long.class) ||
				type.equals(byte.class) || type.equals(int.class) || type.equals(char.class) || type.equals(short.class) ||
				type.equals(String.class) ||
				type.equals(float.class) || type.equals(double.class) || LuaValue.class.isAssignableFrom(type)
			) {
			return true;
		}

		throw new APIBuilder.BuilderException("Cannot validate " + type.getName());
	}

	/**
	 * Injects the validation code for an argument
	 *
	 * @param mv   The method visitor to inject to
	 * @param type The type of the argument
	 */
	@Override
	public void addValidation(MethodVisitor mv, Class<?> type) {
		if (type.equals(boolean.class)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isboolean", "()Z", false);
		} else if (type.equals(byte.class) || type.equals(int.class) || type.equals(char.class) || type.equals(short.class)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isint", "()Z", false);
		} else if (type.equals(float.class) || type.equals(double.class)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isnumber", "()Z", false);
		} else if (type.equals(long.class)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "islong", "()Z", false);
		} else if (type.equals(String.class)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isstring", "()Z", false);
		} else if (LuaValue.class.isAssignableFrom(type)) {
			mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(type));
		} else {
			throw new APIBuilder.BuilderException("Cannot validate " + type.getName());
		}
	}

	/**
	 * Get the type name of the argument
	 *
	 * @param type Argument type
	 * @return The name of the argument type
	 */
	@Override
	public String getName(Class<?> type) {
		String name = CLASS_NAMES.get(type);
		if (name != null) return name;

		if (LuaValue.class.isAssignableFrom(type)) {
			return type.getSimpleName().toLowerCase().replace("lua", "");
		}

		return "anything";
	}

	static {
		Map<Class<?>, String> classNames = CLASS_NAMES = new HashMap<>();
		classNames.put(boolean.class, "boolean");

		classNames.put(byte.class, "number");
		classNames.put(int.class, "number");
		classNames.put(char.class, "number");
		classNames.put(short.class, "number");
		classNames.put(float.class, "number");
		classNames.put(double.class, "number");
		classNames.put(long.class, "number");

		// Cope with LuaDouble/LuaInteger items
		classNames.put(LuaDouble.class, "number");
		classNames.put(LuaInteger.class, "number");

		classNames.put(String.class, "string");
	}
}
