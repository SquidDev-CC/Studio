package org.squiddev.studio.launch;

import com.google.common.io.ByteStreams;
import org.squiddev.patcher.Logger;
import org.squiddev.patcher.transformer.TransformationChain;
import org.squiddev.studio.api.Transformer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom transformation chain with replacing classes in /patch/ directory
 */
public class CustomChain extends TransformationChain implements Transformer {
	protected ArrayList<String> patches = new ArrayList<String>();
	protected final Set<String> exclusions = new HashSet<String>();

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
		if (finalised) throw new IllegalStateException("Cannot add new patchers once finalised");
		patches.add(file);
	}

	@Override
	public void addExclusion(String exclusion) {
		exclusions.add(exclusion);
	}
}
