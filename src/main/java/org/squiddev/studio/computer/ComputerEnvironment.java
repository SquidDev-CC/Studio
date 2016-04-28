package org.squiddev.studio.computer;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import org.squiddev.studio.storage.ComputerInfo;
import org.squiddev.studio.storage.Session;

import java.io.File;
import java.io.IOException;

/**
 * Information about the computer
 */
public class ComputerEnvironment implements IComputerEnvironment {
	public final Session session;
	public final ComputerInfo computerInfo;

	public ComputerEnvironment(Session session, ComputerInfo computerInfo) {
		this.session = session;
		this.computerInfo = computerInfo;
	}

	@Override
	public int getDay() {
		return session.day;
	}

	@Override
	public double getTimeOfDay() {
		return session.time;
	}

	@Override
	public boolean isColour() {
		return computerInfo.advanced;
	}

	@Override
	public long getComputerSpaceLimit() {
		return computerInfo.spaceLimit;
	}

	@Override
	public String getHostString() {
		return "ComputerCraft 1.78 (Studio, CCTweaks)";
	}

	@Override
	public int assignNewID() {
		return session.newId();
	}

	@Override
	public IWritableMount createSaveDirMount(String subPath, long capacity) {
		return new FileMount(new File(session.directory, subPath), capacity);
	}

	@Override
	public IMount createResourceMount(String domain, String subPath) {
		subPath = "assets/" + domain + "/" + subPath;

		try {
			return new JarMount(MountHelpers.mainJar, subPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
