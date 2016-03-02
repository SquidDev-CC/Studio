package org.squiddev.studio.modifications.asm;

import com.google.common.io.ByteStreams;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.TransformationChain;
import org.squiddev.studio.api.Transformer;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Custom transformation chain with replacing classes in /patch/ directory
 */
public class CustomChain extends TransformationChain implements Transformer {
	protected ArrayList<String> patches = new ArrayList<String>();

	@Override
	public byte[] transform(String className, byte[] bytes) throws Exception {
		for (String patch : patches) {
			if (className.startsWith(patch)) {
				String source = "patch/" + className.replace('.', '/') + ".class";

				InputStream stream = CustomChain.class.getClassLoader().getResourceAsStream(source);
				if (stream != null) {
					bytes = ByteStreams.toByteArray(stream);
					break;
				} else {
					Logger.warn("Cannot find custom rewrite " + source);
				}
			}
		}
		return super.transform(className, bytes);
	}

	@Override
	public void addPatchFile(String file) {
		patches.add(file);
	}
}
