package squiddev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import squiddev.ccstudio.computer.Computer;
import squiddev.ccstudio.computer.ComputerEnvironment;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;
import squiddev.ccstudio.core.apis.wrapper.StrictValidator;
import squiddev.ccstudio.core.apis.wrapper.ValidationClass;

/**
 * Handles redstone IO
 */
@SuppressWarnings("UnusedDeclaration")
@LuaAPI({"rs", "redstone"})
@ValidationClass(StrictValidator.class)
public class RedstoneAPI {
	protected final ComputerEnvironment environment;

	public RedstoneAPI(ComputerEnvironment environment) {
		this.environment = environment;
	}

	@LuaFunction
	public LuaTable getSides() {
		return LuaValue.listOf(Computer.SIDE_VALUES);
	}

	@LuaFunction
	public void setOutput(String side, boolean result) {
		environment.redstoneOutput[parseSide(side)] = result ? 0xF : (byte) 0;
	}

	@LuaFunction
	public boolean getOutput(String side) {
		return environment.redstoneOutput[parseSide(side)] > 0;
	}

	@LuaFunction
	public boolean getInput(String side) {
		return environment.redstoneInput[parseSide(side)] > 0;
	}

	@LuaFunction
	public void setBundledOutput(String side, double value) {
		environment.bundledOutput[parseSide(side)] = (int) value;
	}

	@LuaFunction
	public int getBundledOutput(String side) {
		return environment.bundledOutput[parseSide(side)];
	}

	@LuaFunction
	public int getBundledInput(String side) {
		return environment.bundledInput[parseSide(side)];
	}

	@LuaFunction
	public boolean testBundledInput(String side, double value) {
		int mask = (int) value;
		return (environment.bundledInput[parseSide(side)] & mask) == mask;
	}

	@LuaFunction({"setAnalogOutput", "setAnalogueOutput"})
	public void setAnalogueOutput(String side, double value) {
		int output = (int) value;
		if (output < 0 || output > 15) throw new LuaError("Expected number in range 0-15");
		environment.redstoneOutput[parseSide(side)] = (byte) output;
	}

	@LuaFunction({"getAnalogOutput", "getAnalogueOutput"})
	public int getAnalogueOutput(String side) {
		return environment.redstoneOutput[parseSide(side)];
	}

	@LuaFunction({"getAnalogInput", "getAnalogueInput"})
	public int getAnalogueInput(String side) {
		return environment.redstoneInput[parseSide(side)];
	}

	protected int parseSide(String side) {
		Integer index = Computer.SIDE_MAP.get(side);
		if (index == null) throw new LuaError("Invalid side");
		return index;
	}
}
