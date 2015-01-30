package squidev.ccstudio.computer;

import junit.framework.TestCase;
import squidev.ccstudio.core.Config;

public class ComputerTest extends TestCase {

	public void testStart() throws Exception {
		Computer comp = new Computer(new Config());

		comp.start();

	}
}
