package squiddev.ccstudio.computer;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Handles a event to inject into a computer
 */
public class ComputerEvent implements Runnable {
	public final String eventName;
	public final Varargs args;
	public final Computer computer;

	public ComputerEvent(Computer computer, String name, Varargs arguments) {
		this.computer = computer;
		eventName = name;
		this.args = LuaValue.varargsOf(LuaValue.valueOf(name), arguments);
	}

	public ComputerEvent(Computer computer, Varargs arguments) {
		this.computer = computer;
		this.eventName = arguments.arg1().toString();
		this.args = arguments;
	}

	public String name() {
		return eventName;
	}

	public Varargs arguments() {
		return args;
	}

	@Override
	public void run() {
		String filter = computer.filter;
		if (filter == null || filter.equals(eventName)) {
			computer.resume(args);
		}
	}
}
