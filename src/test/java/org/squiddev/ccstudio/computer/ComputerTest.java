package org.squiddev.ccstudio.computer;

import org.junit.Ignore;
import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.squiddev.ccstudio.core.Config;
import org.squiddev.ccstudio.output.terminal.TerminalOutput;

@Ignore
public class ComputerTest {

	@Test
	public void testStart() throws Exception {
		Computer comp = new Computer(new Config(), new TerminalOutput());

		comp.start();
		final Object obj = new Object();

		comp.queueEvent("paste", LuaValue.valueOf("ls"));
		comp.queueEvent("key", LuaValue.valueOf(28));

		comp.events.add(new Runnable() {
			@Override
			public void run() {
				synchronized (obj) {
					obj.notify();
				}
			}
		});

		synchronized (obj) {
			obj.wait();
		}

		comp.shutdown();
	}
}
