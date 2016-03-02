package org.squiddev.studio.modifications;

import org.squiddev.studio.Config;
import org.squiddev.studio.api.Module;
import org.squiddev.studio.api.Transformer;
import org.squiddev.studio.modifications.asm.ASMTransformer;
import org.squiddev.studio.modifications.lua.ApiModule;
import org.squiddev.studio.modifications.lua.LuaEnvironment;

import java.util.HashSet;
import java.util.Set;

/**
 * The main loader
 */
public class Loader {
	private final Set<Module> modules = new HashSet<Module>();

	private final Transformer transformer;
	private final LuaEnvironment environment = LuaEnvironment.instance;

	public Loader(Transformer transformer) {
		this.transformer = transformer;
		modules.add(new ApiModule());
		modules.add(new ASMTransformer());
	}

	public void setup() {
		Config.onSync();

		for (Module module : modules) {
			module.setupTransformer(transformer);
		}

		transformer.finalise();

		for (Module module : modules) {
			module.setupLua(environment);
		}
	}

	public void add(Module module) {
		modules.add(module);
	}
}
