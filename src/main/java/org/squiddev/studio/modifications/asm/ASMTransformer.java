package org.squiddev.studio.modifications.asm;

import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.transformer.ISource;
import org.squiddev.studio.api.Module;
import org.squiddev.studio.api.Transformer;
import org.squiddev.studio.api.lua.ILuaEnvironment;
import org.squiddev.studio.modifications.asm.binary.BinaryUtils;

public class ASMTransformer implements Module {
	protected void add(Transformer patches, Object[] patchers) {
		for (Object patcher : patchers) {
			if (patcher instanceof IPatcher) patches.add((IPatcher) patcher);
			if (patcher instanceof ISource) patches.add((ISource) patcher);
		}
	}

	@Override
	public void setupTransformer(Transformer patches) {
		/*
			TODO: Look into moving some rewrites into compile-time processing instead.
			This probably includes *_Rewrite as well as many of the binary handlers as only exist
			because they need to stub classes that we patch anyway.
		 */
		patches.addPatchFile("org.luaj.vm2.lib.DebugLib");
		patches.addPatchFile("org.luaj.vm2.lib.StringLib");

		add(patches, new Object[]{
			new CustomTimeout(),
			new InjectLuaJC(),
			new WhitelistGlobals(),
			new CustomAPIs(),

			new ClassMerger(
				"dan200.computercraft.core.apis.PeripheralAPI",
				"org.squiddev.studio.modifications.patch.PeripheralAPI_Patch"
			),
			new AddAdditionalData(),
		});

		BinaryUtils.inject(patches);
	}

	@Override
	public void setupLua(ILuaEnvironment environment) {

	}
}
