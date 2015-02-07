package org.luaj.vm2.luajc;

import org.luaj.vm2.LocVars;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Dumps the contents of a Lua prototype to a readable format
 * TODO: This is in totally the wrong place
 */
public class DumpInstructions {
	protected static final Map<Integer, String> opcodes;

	public static void dump(Prototype proto, PrintWriter writer) {
		writer.println("Lua source " + proto.source);
		writer.println("\tArgs      " + proto.numparams);
		writer.println("\tIs vararg " + (proto.is_vararg >= 2 ? "y" : "n"));

		writer.println("Constants");
		{
			int counter = 0;
			for (LuaValue constant : proto.k) {
				writer.println("\t" + counter + ": " + constant.typename() + " " + constant.toString());
				++counter;
			}
		}

		writer.println("Code");
		{
			int counter = 0;
			for (int ins : proto.code) {
				final int opcode = Lua.GET_OPCODE(ins);
				int a = Lua.GETARG_A(ins);
				int b = Lua.GETARG_B(ins);
				int bx = Lua.GETARG_Bx(ins);
				int sbx = Lua.GETARG_sBx(ins);
				int c = Lua.GETARG_C(ins);

				// TODO: Print only the useful opcodes
				writer.println("\t" + counter + ": " + opcodes.get(opcode) + " " + a + " " + b + " " + bx + " " + sbx + " " + c);

				++counter;
			}
		}
		writer.println("Locals");
		{
			for (LocVars var : proto.locvars) {
				writer.println("\t " + var.varname);
			}
		}

		writer.flush();
	}

	public static void dump(Prototype proto) {
		dump(proto, new PrintWriter(System.out));
	}

	static {
		HashMap<Integer, String> ops = new HashMap<>();
		opcodes = ops;

		try {
			for (Field f : Lua.class.getFields()) {
				if (f.getName().startsWith("OP_")) {
					ops.put(f.getInt(null), f.getName().substring(3));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getOpcode(int instruction) {
		return opcodes.get(Lua.GET_OPCODE(instruction));
	}
}
