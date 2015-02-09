package squidev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.apis.wrapper.LuaAPI;
import squidev.ccstudio.core.apis.wrapper.LuaFunction;
import squidev.ccstudio.core.peripheral.IPeripheral;

/**
 * Handles peripheral methods
 */
@SuppressWarnings("UnusedDeclaration")
@LuaAPI("peripheral")
public class PeripheralAPI {
	IPeripheral[] peripherals = new IPeripheral[6];

	@LuaFunction
	public boolean isPresent(String side) {
		return getPeripheral(side) != null;
	}

	@LuaFunction
	public String getType(String side) {
		IPeripheral peripheral = getPeripheral(side);
		return peripheral == null ? null : peripheral.getType();
	}

	@LuaFunction
	public Varargs getMethods(String side) {
		IPeripheral peripheral = getPeripheral(side);
		return peripheral == null ? LuaValue.NONE : LuaValue.listOf(peripheral.getTable().keys());
	}

	@LuaFunction
	public Varargs call(Varargs args) {
		if (args.narg() >= 2 && args.arg(1).isstring() && args.arg(2).isstring()) {
			// TODO: Dynamically build the peripheral wrapper instead. This is not a great method
			IPeripheral peripheral = getPeripheral(args.arg(1).toString());
			if (peripheral == null) throw new LuaError("No peripheral attached");

			String methodName = args.arg(2).toString();
			LuaValue method = peripheral.getTable().get(methodName);
			if (method == null || method == LuaValue.NIL) throw new LuaError("No such method " + methodName);
			return method.invoke(args.subargs(3));
		}

		throw new LuaError("Expected string, string");
	}

	public IPeripheral getPeripheral(String side) {
		Integer sideNumber = Computer.SIDE_MAP.get(side);
		return sideNumber == null ? null : peripherals[sideNumber];
	}
}
