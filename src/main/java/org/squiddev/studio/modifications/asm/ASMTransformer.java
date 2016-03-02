package org.squiddev.studio.modifications.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.ClassMerger;
import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.transformer.ISource;
import org.squiddev.studio.modifications.Config;
import org.squiddev.studio.modifications.asm.binary.BinaryUtils;

import java.io.*;

public class ASMTransformer {
	public final CustomChain patches = new CustomChain();

	protected void add(Object[] patchers) {
		for (Object patcher : patchers) {
			if (patcher instanceof IPatcher) patches.add((IPatcher) patcher);
			if (patcher instanceof ISource) patches.add((ISource) patcher);
		}
	}

	private final File dumpFolder;

	public ASMTransformer() {
		dumpFolder = new File("asm-studio");
		if (Config.Testing.dumpAsm && !dumpFolder.exists() && !dumpFolder.mkdirs()) {
			Logger.warn("Cannot create ASM dump folder");
		}

		/*
			TODO: Look into moving some rewrites into compile-time processing instead.
			This probably includes *_Rewrite as well as many of the binary handlers as only exist
			because they need to stub classes that we patch anyway.
		 */
		patches.addPatchFile("org.luaj.vm2.lib.DebugLib");
		patches.addPatchFile("org.luaj.vm2.lib.StringLib");

		add(new Object[]{
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

	public byte[] transform(String className, byte[] bytes) {
		try {
			byte[] rewritten = patches.transform(className, bytes);
			if (rewritten != bytes) writeDump(className, rewritten);
			return rewritten;
		} catch (Exception e) {
			Logger.error("Cannot patch " + className + ", falling back to default", e);
			return bytes;
		}
	}

	public void dump(String className, byte[] bytes) {
		StringWriter writer = new StringWriter();
		new ClassReader(bytes).accept(new TraceClassVisitor(new PrintWriter(writer)), 0);
		Logger.debug("Dump for " + className + "\n" + writer.toString());
	}

	public void writeDump(String className, byte[] bytes) {
		if (Config.Testing.dumpAsm) {
			File file = new File(dumpFolder, className.replace('.', '/') + ".class");
			File directory = file.getParentFile();
			if (directory.exists() || directory.mkdirs()) {
				try {
					OutputStream stream = new FileOutputStream(file);
					try {
						stream.write(bytes);
					} catch (IOException e) {
						Logger.error("Cannot write " + file, e);
					} finally {
						stream.close();
					}
				} catch (FileNotFoundException e) {
					Logger.error("Cannot write " + file, e);
				} catch (IOException e) {
					Logger.error("Cannot write " + file, e);
				}
			} else {
				Logger.warn("Cannot create folder for " + file);
			}
		}
	}
}
