package squidev.ccstudio.computer;

import org.junit.Test;
import squidev.ccstudio.core.Config;

public class ComputerTest {

	@Test
	public void testStart() throws Exception {
		Computer comp = new Computer(new Config());

		comp.start();
		Object obj = new Object();

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
