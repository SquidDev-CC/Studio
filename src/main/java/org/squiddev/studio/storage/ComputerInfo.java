package org.squiddev.studio.storage;

/**
 * Data about a computer
 */
public class ComputerInfo {
	public static final long DEFAULT_SPACE_LIMIT = 1048576;
	public static final long MAX_SPACE_LIMIT = 134217728;

	/**
	 * The label for the computer
	 */
	public String label;

	/**
	 * The space limit of the computer
	 */
	public long spaceLimit = DEFAULT_SPACE_LIMIT;

	/**
	 * The ID for the computer
	 */
	public int id;

	/**
	 * If the computer is advanced.
	 */
	public boolean advanced = true;

	/**
	 * Information about each side
	 */
	public Side[] sides;

	/**
	 * Width of the terminal
	 */
	public int termWidth = 51;

	/**
	 * Height of the terminal
	 */
	public int termHeight = 19;

	public ComputerInfo() {
		sides = new Side[6];
		for (int i = 0; i < 6; i++) {
			sides[i] = new Side();
		}
	}
}
