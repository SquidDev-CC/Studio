package squiddev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;

/**
 * The main Bit API
 */
@SuppressWarnings("UnusedDeclaration")
@LuaAPI("bit")
public class BitAPI {
	protected static int toNumber(LuaValue o) {
		if (o instanceof LuaNumber) { // Sadly CC does not support passing strings
			double d = o.todouble();
			if (d % 1.0D != 0.0D) {
				throw new LuaError("passed number is not an integer");
			} else if (d >= 4.294967296E9D) {
				throw new LuaError("number is too large (maximum allowed: 2^32-1)");
			} else {
				return (int) ((long) d);
			}
		} else if (o == LuaValue.NIL) {
			throw new LuaError("too few arguments");
		} else {
			throw new LuaError("number expected");
		}
	}

	@LuaFunction
	public int bnot(Varargs args) {
		return ~toNumber(args.arg1());
	}

	@LuaFunction
	public int band(Varargs args) {
		return toNumber(args.arg(1)) & toNumber(args.arg(2));
	}

	@LuaFunction
	public int bor(Varargs args) {
		return toNumber(args.arg(1)) | toNumber(args.arg(2));
	}

	@LuaFunction
	public int bxor(Varargs args) {
		return toNumber(args.arg(1)) ^ toNumber(args.arg(2));
	}

	@LuaFunction
	public int brshift(Varargs args) {
		return toNumber(args.arg(1)) >> toNumber(args.arg(2));
	}

	@LuaFunction
	public int blshift(Varargs args) {
		return toNumber(args.arg(1)) << toNumber(args.arg(2));
	}

	@LuaFunction
	public int blogic_rshift(Varargs args) {
		return toNumber(args.arg(1)) >>> toNumber(args.arg(2));
	}
}
