package squidev.ccstudio.computer;

import org.junit.Ignore;
import org.junit.Test;
import org.luaj.vm2.LuaValue;
import squidev.ccstudio.core.Config;
import squidev.ccstudio.output.terminal.TerminalOutput;

@Ignore
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
