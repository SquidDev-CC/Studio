package squiddev.ccstudio.computer;

import org.junit.Test;
import org.luaj.vm2.LuaValue;
import squiddev.ccstudio.core.Config;
import squiddev.ccstudio.output.terminal.TerminalOutput;

public class ComputerTest {

	@Test
	public void testStart() throws Exception {
		Computer comp = new Computer(new Config(), new TerminalOutput());

		comp.start();
		Object obj = new Object();

		comp.queueEvent("paste", LuaValue.valueOf("ls"));
		comp.queueEvent("key", LuaValue.valueOf(28));

		comp.events.add(() -> {
			synchronized (obj) {
				obj.notify();
			}
		});

		synchronized (obj) {
			obj.wait();
		}

		comp.shutdown();
	}
}
