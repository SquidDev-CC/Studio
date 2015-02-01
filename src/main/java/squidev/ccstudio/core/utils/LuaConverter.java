package squidev.ccstudio.core.utils;

import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * squidev.ccstudio.core.utils (CCStudio.Java
 */
public class LuaConverter {
	/**
	 * Holds objects being worked on currently
	 */
	public Map<LuaValue, Object> processingObjects;

	/**
	 * Convert a LuaValue to a Java value
	 *
	 * @param value The value to convert
	 * @return The converted value or null or failure
	 */
	public Object toObject(LuaValue value) {
		switch (value.type()) {
			case LuaValue.TNONE:
			case LuaValue.TNIL:
				return null;
			case LuaValue.TNUMBER:
			case LuaValue.TINT:
				return value.todouble();
			case LuaValue.TBOOLEAN:
				return value.toboolean();
			case LuaValue.TSTRING:
				return value.toString();
			case LuaValue.TTABLE:
				boolean clearWhenDone = false;
				if (processingObjects == null) {
					processingObjects = new IdentityHashMap<LuaValue, Object>();
					clearWhenDone = true;
				} else if (processingObjects.containsKey(value)) {
					return processingObjects.get(value);
				} else {
					Map<Object, Object> map = new HashMap<Object, Object>();
					processingObjects.put(value, map);

					// value.enum
				}
		}

		return null;
	}
}
