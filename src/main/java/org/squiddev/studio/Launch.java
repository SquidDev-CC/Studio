package org.squiddev.studio;

import java.util.Arrays;
import org.squiddev.cctweaks.lua.launch.RewritingLoader;

import static org.squiddev.cctweaks.lua.launch.Launcher.*;

/**
 * The main launcher
 */
public class Launch {
	public static void main(String[] args) throws Exception {
		String type = "laterna";
		if (args.length > 0) type = args[0];

		RewritingLoader loader = setupLoader();
		loader.chain.finalise();
		execute(loader, 
			"org.squiddev.studio.interact." + type + ".Runner", 
			args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : args
		);
	}
}