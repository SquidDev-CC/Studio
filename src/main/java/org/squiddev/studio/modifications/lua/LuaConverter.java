package org.squiddev.studio.modifications.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Duplicate of {@link dan200.computercraft.core.lua.LuaJLuaMachine#toObject(LuaValue)} with
 * binary support
 */
public class LuaConverter {
	public static Object toObject(LuaValue value, boolean binary) {
		return toObject(value, null, binary);
	}

	private static Object toObject(LuaValue value, Map<LuaValue, Object> tables, boolean binary) {
		switch (value.type()) {
			case LuaValue.TNUMBER:
			case LuaValue.TINT:
				return value.todouble();
			case LuaValue.TBOOLEAN:
				return value.toboolean();
			case LuaValue.TSTRING: {
				LuaString string = (LuaString) value;
				if (binary) {
					byte[] result = new byte[string.m_length];
					System.arraycopy(string.m_bytes, string.m_offset, result, 0, string.m_length);
					return result;
				} else {
					return decodeString(string.m_bytes, string.m_offset, string.m_length);
				}
			}
			case LuaValue.TTABLE: {
				if (tables == null) {
					tables = new IdentityHashMap<LuaValue, Object>();
				} else {
					Object object = tables.get(value);
					if (object != null) return object;
				}

				Map<Object, Object> table = new HashMap<Object, Object>();
				tables.put(value, table);

				LuaValue k = LuaValue.NIL;
				while (true) {
					Varargs keyValue = value.next(k);
					k = keyValue.arg1();
					if (k.isnil()) break;

					LuaValue v = keyValue.arg(2);
					Object keyObject = toObject(k, tables, binary);
					Object valueObject = toObject(v, tables, binary);
					if (keyObject != null && valueObject != null) {
						table.put(keyObject, valueObject);
					}
				}
				return table;
			}
			default:
				return null;
		}
	}

	public static Object[] toObjects(Varargs values, int start, boolean binary) {
		int count = values.narg();
		Object[] objects = new Object[count - start + 1];
		for (int n = start; n <= count; n++) {
			int i = n - start;
			LuaValue value = values.arg(n);
			objects[i] = toObject(value, null, binary);
		}
		return objects;
	}

	public static Object toString(Object value) {
		return toString(value, null);
	}

	private static Object toString(Object value, Map<Object, Object> tables) {
		if (value instanceof byte[]) {
			return new String((byte[]) value);
		} else if (value instanceof Map) {
			if (tables == null) {
				tables = new IdentityHashMap<Object, Object>();
			} else {
				Object object = tables.get(value);
				if (object != null) return object;
			}

			Map<Object, Object> newMap = new HashMap<Object, Object>();
			tables.put(value, newMap);

			Map<?, ?> map = (Map) value;

			for (Object key : map.keySet()) {
				newMap.put(toString(key, tables), toString(map.get(key), tables));
			}

			return newMap;
		} else {
			return value;
		}
	}


	/**
	 * Convert the arguments to use strings instead of byte arrays
	 *
	 * @param items The arguments to convert. This will be modified in place
	 */
	public static void toStrings(Object[] items) {
		for (int i = 0; i < items.length; i++) {
			items[i] = toString(items[i], null);
		}
	}

	public static String decodeString(byte[] bytes) {
		return decodeString(bytes, 0, bytes.length);
	}

	public static String decodeString(byte[] bytes, int start, int length) {
		char[] chars = new char[length];

		for (int i = 0; i < chars.length; ++i) {
			chars[i] = (char) (bytes[start + i] & 255);
		}

		return new String(chars);
	}

	public static byte[] toBytes(String string) {
		byte[] chars = new byte[string.length()];

		for (int i = 0; i < chars.length; ++i) {
			// chars[i] = (char) (bytes[start + i] & 255);
		}

		return chars;
	}
}
