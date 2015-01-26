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
		return peripheral == null ? LuaValue.NONE : LuaValue.listOf(peripheral.getObject().getTable().keys());
	}

	@LuaFunction
	public Varargs call(String side, String methodName, Varargs args) {
		IPeripheral peripheral = getPeripheral(side);
		if (peripheral == null) throw new LuaError("No peripheral attached");

		LuaValue method = peripheral.getObject().getTable().get(methodName);
		if (method == null || method == LuaValue.NIL) throw new LuaError("No such method " + methodName);
		return method.invoke(args);
	}

	public IPeripheral getPeripheral(String side) {
		Integer sideNumber = Computer.SIDE_MAP.get(side);
		return sideNumber == null ? null : peripherals[sideNumber];
	}
}
