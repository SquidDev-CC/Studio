package squiddev.ccstudio.core.apis.wrapper.builder;

import org.luaj.vm2.Varargs;
import squiddev.ccstudio.core.apis.wrapper.DefaultLuaValidator;
import squiddev.ccstudio.core.apis.wrapper.ILuaValidator;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;
import squiddev.ccstudio.core.apis.wrapper.ValidationClass;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Stores all data associated with a Lua function
 */
public class LuaMethod implements Iterable<LuaMethod.LuaArgument> {
	public final Class<?> type;
	public final LuaFunction function;
	public final Method method;

	public final boolean varargs;

	protected final LuaArgument[] arguments;

	public LuaMethod(Method m) {
		function = m.getAnnotation(LuaFunction.class);
		method = m;
		type = m.getDeclaringClass();


		boolean varargs = false;
		Parameter[] params = m.getParameters();
		LuaArgument[] arguments = this.arguments = new LuaArgument[params.length];
		for (int i = 0; i < params.length; i++) {
			Parameter param = params[i];
			if(param.getType().equals(Varargs.class)) {
				varargs = true;
				if (i + 1 < params.length) {
					throw new APIBuilder.BuilderException("Varargs must be last item", m);
				}
			}


			arguments[i] = new LuaArgument(param);
		}

		this.varargs = varargs;
	}

	/**
	 * Get the names of the Lua function
	 *
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
	 *
	 * @return The Java method name
	 */
	public String getJavaName() {
		return method.getName();
	}

	/**
	 * Get the error to throw
	 *
	 * @return The error message to throw or null on nothing
	 */
	public String getError() {
		String error = function.error();
		return (error == null || error.isEmpty()) ? null : error;
	}

	@Override
	public Iterator<LuaArgument> iterator() {
		return validationIterator();
	}

	public ValidationIterator validationIterator() {
		return new ValidationIterator();
	}

	/**
	 * Stores one argument of a Lua method
	 */
	public static class LuaArgument {
		private static final Map<Class<? extends ILuaValidator>, ILuaValidator> VALIDATORS = new HashMap<>();

		public final Class<?> type;
		public final Class<? extends ILuaValidator> validatorType;

		public LuaArgument(Parameter parameter) {
			this(parameter.getType(), getValidator(parameter));
		}

		public LuaArgument(Class<?> type, Class<? extends ILuaValidator> validator) {
			this.type = type;
			validatorType = validator;
		}

		public ILuaValidator getValidator() {
			ILuaValidator val = VALIDATORS.get(validatorType);

			if (val == null) {
				try {
					val = validatorType.newInstance();
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("Cannot create new instance of " + validatorType.getName(), e);
				}

				VALIDATORS.put(validatorType, val);
			}

			return val;
		}

		protected static Class<? extends ILuaValidator> getValidator(Parameter parameter) {
			ValidationClass validator = parameter.getAnnotation(ValidationClass.class);
			if (validator != null) return validator.value();

			validator = parameter.getDeclaringExecutable().getAnnotation(ValidationClass.class);
			if (validator != null) return validator.value();

			validator = parameter.getDeclaringExecutable().getDeclaringClass().getAnnotation(ValidationClass.class);
			if (validator != null) return validator.value();

			return DefaultLuaValidator.class;
		}
	}

	/**
	 * An iterator that checks if there is another item to be validated
	 */
	public class ValidationIterator implements Iterator<LuaArgument>, Iterable<LuaArgument> {
		private int index = 0;
		private final LuaArgument[] items;

		public ValidationIterator() {
			items = arguments;
		}

		@Override
		public boolean hasNext() {
			return index < items.length;
		}

		/**
		 * Checks if there is another item to be validated
		 *
		 * @return If there is another item
		 */
		public boolean hasValidateNext() {
			LuaArgument[] items = this.items;
			for (int i = index; i < items.length; i++) {
				LuaArgument arg = items[i];
				if (arg.getValidator().shouldValidate(arg.type)) return true;
			}

			return false;
		}

		@Override
		public LuaArgument next() {
			return items[index++];
		}

		public int length() {
			return items.length - (varargs ? 1 : 0);
		}

		public void rewind() {
			index = 0;
		}

		@Override
		public Iterator<LuaArgument> iterator() {
			return this;
		}
	}
}

