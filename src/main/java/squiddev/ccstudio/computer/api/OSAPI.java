package squiddev.ccstudio.computer.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.ccstudio.computer.Computer;
import squiddev.ccstudio.computer.ComputerEnvironment;
import squiddev.ccstudio.core.apis.wrapper.LuaAPI;
import squiddev.ccstudio.core.apis.wrapper.LuaFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles the main OS code
 */
//@SuppressWarnings("UnusedDeclaration")
@LuaAPI("os")
public class OSAPI {
	public final Computer computer;
	public final ComputerEnvironment environment;
	protected final Map<Integer, ComputerTimer> timers = new HashMap<>();
	protected final Map<Integer, ComputerAlarm> alarms = new HashMap<>();
	/**
	 * Create a background thread to run the OS API time based functions.
	 * This includes Alarms and Timers
	 * <p>
	 * TODO: This needs to be turned off when the computer turns off
	 */
	protected final Timer timer = new Timer(true);
	protected int timerId = 0;
	protected int alarmId = 0;

	public OSAPI(Computer computer) {
		this.computer = computer;
		environment = computer.environment;
	}

	@LuaFunction
	public void queueEvent(Varargs args) {
		if (!args.arg1().isstring()) throw new LuaError("Expected string");
		// TODO: Clone tables and trim at functions
		// This is because CC converts to/from LuaValues so the original objects are lost
		computer.queueEvent(args);
	}

	@LuaFunction
	public int startTimer(double delay) {
		ComputerTimer task = new ComputerTimer();
		// x 1000 to convert to milliseconds, clamp to being positive
		timer.schedule(task, (long) Math.max(0, delay * 1000));
		timers.put(task.id, task);
		return task.id;
	}

	@LuaFunction
	public int setAlarm(double time) {
		if (time < 0 || time >= 24) throw new LuaError("Number out of range");

		double currentTime = time();
		double difference = (currentTime - time) % 24;
		if (difference < 0) difference += 24;

		// Convert 24 hour clock in to milliseconds
		ComputerAlarm task = new ComputerAlarm();
		timer.schedule(task, (long) difference / 24 * 1200 * 1000);
		alarms.put(task.id, task);
		return task.id;
	}

	@LuaFunction
	public void shutdown() {
		computer.shutdown();
	}

	@LuaFunction
	public void reboot() {
		throw new UnsupportedOperationException();
	}

	@LuaFunction({"getComputerId", "computerId"})
	public int getComputerId() {
		return environment.id;
	}

	@LuaFunction({"getComputerLabel", "computerLabel"})
	public LuaValue getComputerLabel() {
		String label = environment.label;
		if (label == null) return LuaValue.NIL;
		return LuaValue.valueOf(label);
	}

	@LuaFunction
	public void setComputerLabel(Varargs args) {
		LuaValue arg = args.arg1();
		if (arg instanceof LuaString) {
			String label = arg.tojstring();
			if (label.length() > 32) label = label.substring(0, 32);
			environment.label = label;
		} else if (arg.isnil()) {
			environment.label = null;
		} else {
			throw new LuaError("Expected string or nil");
		}
	}

	@LuaFunction
	public double clock() {
		// Round to 2d.p
		return Math.round((System.currentTimeMillis() - environment.startTime) / 10) / 100;
	}

	@LuaFunction
	public int day() {
		return (int) ((double) (System.currentTimeMillis() - environment.startTime) / 1000 / 1200 / 24);
	}

	@LuaFunction
	public double time() {
		return Math.floor((double) (System.currentTimeMillis() - environment.startTime) / 1000) / 1200 % 24;
	}

	@LuaFunction
	public void cancelTimer(int id) {
		ComputerTimer timer = timers.get(id);
		if (timer != null) timer.active = false;
	}

	@LuaFunction
	public void cancelAlarm(int id) {
		ComputerAlarm alarm = alarms.get(id);
		if (alarm != null) alarm.active = false;
	}

	/**
	 * Handles a computer timer
	 */
	public class ComputerTimer extends TimerTask {
		public final int id = ++timerId;
		public boolean active = true;

		@Override
		public void run() {
			if (active) OSAPI.this.computer.queueEvent("timer", LuaValue.valueOf(id));
			timers.remove(id);
		}
	}

	/**
	 * Handles a computer alarm.
	 * This is scheduled for a time on the computer instead
	 */
	public class ComputerAlarm extends TimerTask {
		public final int id = ++alarmId;
		public boolean active = true;

		@Override
		public void run() {
			if (active) OSAPI.this.computer.queueEvent("alarm", LuaValue.valueOf(id));
			alarms.remove(id);
		}
	}
}
