package org.squiddev.studio.api;

import org.squiddev.patcher.transformer.IPatcher;
import org.squiddev.patcher.transformer.ISource;

/**
 * A basic transformer implementation
 */
public interface Transformer {
	/**
	 * Add a class to patch through the /patch/ directory
	 *
	 * @param file the class to patch
	 */
	void addPatchFile(String file);

	void add(IPatcher patcher);

	void add(ISource source);

	void addExclusion(String exclusion);

	void finalise();
}
