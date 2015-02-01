package squidev.ccstudio.__ignore;

import java.util.HashMap;
import java.util.Map;

/**
 * squidev.ccstudio.__ignore (CCStudio.Java
 */
public class PerformanceChecker {
	public static final Map<Integer, Integer> conversion = new HashMap<Integer, Integer>();

	static {
		for (int i = 0; i < 16; i++) {
			conversion.put(i, i * 10);
		}
	}

	public static int map(int color) {
		switch (color) {
			case 0:
				return 0;
			case 1:
				return 10;
			case 2:
				return 20;
			case 3:
				return 30;
			case 4:
				return 40;
			case 5:
				return 50;
			case 6:
				return 60;
			case 7:
				return 70;
			case 8:
				return 80;
			case 9:
				return 90;
			case 10:
				return 100;
			case 11:
				return 110;
			case 12:
				return 120;
			case 13:
				return 130;
			case 14:
				return 140;
			case 15:
				return 150;
			default:
				return 0;
		}
	}

	public static void main(String[] args) {
		for (int k = 0; k < 2; k++) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < 10000000; i++) {
				for (int j = 0; j < 16; j++) {
					int a = conversion.get(j);
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("HashMap = " + ((end - start)) + " millisecond");

			start = System.currentTimeMillis();
			for (int i = 0; i < 10000000; i++) {
				for (int j = 0; j < 16; j++) {
					int a = map(j);
				}
			}
			end = System.currentTimeMillis();
			System.out.println("Switch = " + ((end - start)) + " millisecond");
		}

	}
}
