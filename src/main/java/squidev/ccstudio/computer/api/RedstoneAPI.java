package squidev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.apis.wrapper.LuaAPI;
import squidev.ccstudio.core.apis.wrapper.LuaFunction;

/**
 * Handles redstone IO
 */
@SuppressWarnings("UnusedDeclaration")
@LuaAPI({"rs", "redstone"})
public class RedstoneAPI {
	protected final Computer computer;

	public RedstoneAPI(Computer computer) {
		this.computer = computer;
	}

	// "getSides", "setOutput", "getOutput", "getInput", "setBundledOutput", "getBundledOutput", "getBundledInput", "testBundledInput", "setAnalogOutput", "setAnalogueOutput", "getAnalogOutput", "getAnalogueOutput", "getAnalogInput", "getAnalogueInput"
	@LuaFunction
	public LuaTable getSides() {
		return LuaValue.listOf(Computer.SIDE_VALUES);
	}

	@LuaFunction
	public void setOutput(String side, boolean output) {
		Integer index = Computer.SIDE_MAP.get(side);
		if (index == null) throw new LuaError("Expected string, boolean");
		computer.environment.redstoneOutput[index] = output ? 0xF : (byte) 0;
	}

	protected int parseSide(String side) {
		Integer index = Computer.SIDE_MAP.get(side);
		if (index == null) throw new LuaError("Invalid side");
		return index;
	}
}
