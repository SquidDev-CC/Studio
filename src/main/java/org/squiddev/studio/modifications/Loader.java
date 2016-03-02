package org.squiddev.studio.modifications;

import org.squiddev.patcher.Logger;
import org.squiddev.studio.api.Module;
import org.squiddev.studio.modifications.asm.ASMTransformer;
import org.squiddev.studio.modifications.lua.ApiModule;
import org.squiddev.studio.modifications.lua.LuaEnvironment;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * The main loader
 */
public class Loader implements ClassFileTransformer {
	private Set<Module> modules = new HashSet<Module>();

	private final ASMTransformer transformer = new ASMTransformer();
	private final LuaEnvironment environment = LuaEnvironment.instance;

	public Loader() {
		modules.add(new ApiModule());
	}

	public void setup(Instrumentation inst) {
		for (Module module : modules) {
			module.setupTransformer(transformer.patches);
		}

		transformer.patches.finalise();
		inst.addTransformer(this);

		for (Module module : modules) {
			module.setupLua(environment);
		}
	}

	public void add(Module module) {
		modules.add(module);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.startsWith("dan200/computercraft")) {
			return transformer.transform(className.replace('/', '.'), classfileBuffer);
		}

		return classfileBuffer;
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		Loader loader = new Loader();

		if (agentArgs != null) {
			for (String module : agentArgs.split("[,;]")) {
				try {
					loader.add((Module) Loader.class.getClassLoader().loadClass(module).newInstance());
				} catch (Exception e) {
					Logger.error("Cannot load " + module, e);
				}
			}
		}

		Config.onSync();
		loader.setup(inst);
	}
}
