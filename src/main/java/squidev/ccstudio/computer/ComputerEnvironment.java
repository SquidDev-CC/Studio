package squidev.ccstudio.computer;

import java.io.Serializable;

/**
 * Stores data about the computer
 */
public class ComputerEnvironment implements Serializable {
	/**
	 * Normal redstone input states
	 */
	public final byte[] redstoneInput = new byte[6];
	/**
	 * Normal redstone output states
	 */
	public final byte[] redstoneOutput = new byte[6];
	/**
	 * Bundled cables input states
	 */
	public final int[] bundledInput = new int[6];
	/**
	 * Bundled cables output states
	 */
	public final int[] bundledOutput = new int[6];
	public String label = null;
	public int id = 0;
	/**
	 * The time in milliseconds that this clock started
	 */
	public long startTime = System.currentTimeMillis();
}
