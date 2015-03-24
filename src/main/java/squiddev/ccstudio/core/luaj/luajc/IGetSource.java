package squiddev.ccstudio.core.luaj.luajc;

import org.luaj.vm2.Prototype;

/**
 * Interface used to get current debugging info
 */
public interface IGetSource {
	String getSource();

	int getLine();

	Prototype getPrototype();
}