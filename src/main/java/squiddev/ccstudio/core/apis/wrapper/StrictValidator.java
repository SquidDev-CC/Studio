package squiddev.ccstudio.core.apis.wrapper;

import org.luaj.vm2.LuaValue;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import squiddev.ccstudio.core.apis.wrapper.builder.APIBuilder;

import static org.objectweb.asm.Opcodes.INSTANCEOF;

/**
 * Validates values using <code>instanceof</code>
 * <p>
 * All number types are compared against {@see LuaNumber}
 */
public class StrictValidator extends DefaultLuaValidator {
	/**
	 * Injects the validation code for an argument
	 *
	 * @param mv   The method visitor to inject to
	 * @param type The type of the argument
	 */
	@Override
	public void addValidation(MethodVisitor mv, Class<?> type) {
		if (type.equals(boolean.class)) {
			mv.visitTypeInsn(INSTANCEOF, "org/luaj/vm2/LuaBoolean");
		} else if (
			type.equals(byte.class) || type.equals(int.class) || type.equals(char.class) || type.equals(short.class) ||
				type.equals(float.class) || type.equals(double.class) || type.equals(long.class)
			) {
			mv.visitTypeInsn(INSTANCEOF, "org/luaj/vm2/LuaNumber");
		} else if (type.equals(String.class)) {
			mv.visitTypeInsn(INSTANCEOF, "org/luaj/vm2/Sting");
		} else if (LuaValue.class.isAssignableFrom(type)) {
			mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(type));
		} else {
			throw new APIBuilder.BuilderException("Cannot validate " + type.getName());
		}
	}
}
