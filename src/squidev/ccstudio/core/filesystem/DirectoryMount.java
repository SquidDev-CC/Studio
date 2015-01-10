package squidev.ccstudio.core.filesystem;

import squidev.ccstudio.core.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mounts to a directory on the filesystem
 */
public class DirectoryMount implements IMount {
	public String base;
	public String label;
	public boolean readOnly;
	public int maxSpace = MAX_SPACE;

	public Config config;

	private long usedSpace = 0L;

	public DirectoryMount(String base, boolean readOnly, String label, Config config) {
		this.base = base;
		this.label = label;
		this.readOnly = readOnly;

		this.config = config;
		if(caresAboutSpace()) usedSpace = measureUsedSpace(new File(base));
	}

	@Override
	public String getLabel(FilePath filePath) {
		return label;
	}

	@Override
	public boolean isReadOnly(FilePath filePath) {
		return readOnly;
	}

	@Override
	public boolean exists(FilePath filePath) {
		return realPath(filePath).exists();
	}

	@Override
	public boolean isDirectory(FilePath filePath) {
		return realPath(filePath).isDirectory();
	}

	@Override
	public Iterable<FilePath> list(FilePath filePath) throws IOException {
		File real = realPath(filePath);
		if(!real.exists()) throw new IOException("Not a directory");

		FilePath realFilePath = new FilePath(real.getPath());

		List<FilePath> contents = new ArrayList<FilePath>();
		for(String sub : real.list()) {
			if(new File(real, sub).exists()) contents.add(new FilePath(sub));
		}

		return contents;
	}

	@Override
	public long getSize(FilePath filePath) throws IOException {
		File real = realPath(filePath);
		if(!real.exists()) throw new IOException("No such file");
		if(real.isDirectory()) return 0L;

		return real.length();
	}

	@Override
	public long getRemainingSpace(FilePath filePath) {
		return getRemainingSpace();
	}

	public long getRemainingSpace() {
		if(readOnly) return 0L;
		if(config.storageLimit) return config.remainingSpace;
		return Math.max(maxSpace - usedSpace, 0);
	}

	@Override
	public InputStream read(FilePath FileP) {
		return null;
	}

	@Override
	public OutputStream write(FilePath FileP) {
		return null;
	}

	@Override
	public OutputStream append(FilePath filePath) {
		return null;
	}

	@Override
	public void makeDirectory(FilePath filePath) throws IOException {
		if(readOnly) throw new IllegalArgumentException(ACCESS_DENIED);

		File real = realPath(filePath);
		if(real.exists()) {
			if(real.isFile()) throw new IllegalArgumentException("File exists");
		} else {
			if(config.storageLimit && getRemainingSpace() < MINIMUM_FILE_SIZE) throw new IllegalArgumentException("Out of space");

			if(real.mkdirs()) {
				usedSpace += MINIMUM_FILE_SIZE;
			} else {
				throw new IOException(ACCESS_DENIED);
			}
		}

	}
	@Override
	public void delete(FilePath filePath) throws IOException {
		if(readOnly || filePath.isRoot()) throw new IOException(ACCESS_DENIED);

		File file = realPath(filePath);
		if(file.exists()) {
			deleteRecursivly(file);
		}
	}

	protected void deleteRecursivly(File file)
		throws IOException
	{
		long size;
		if(file.isDirectory()) {
			for(File sub : file.listFiles()) {
				deleteRecursivly(sub);
			}

			size = 0L;
		} else {
			size = file.length();
		}

		if(file.delete()) {
			if(config.storageLimit) usedSpace -= Math.max(MINIMUM_FILE_SIZE, size);
		} else {
			throw new IOException(ACCESS_DENIED);
		}
	}

	/**
	 * Create the real path
	 * @param file The filepath to use
	 * @return The path on the current filesystem
	 */
	protected File realPath(FilePath file)
	{
		return new File(base, file.getPath().replace(FilePath.DIRECTORY_SEPARATOR, File.separator));
	}

	/**
	 * Calculates the used space
	 * @param file The file to calculate
	 * @return The currently used space
	 */
	protected long measureUsedSpace(File file)
	{
		if (!file.exists()) return 0L;
		if (file.isDirectory())
		{
			long size = MINIMUM_FILE_SIZE;
			for (File sub : file.listFiles())
			{
				size += measureUsedSpace(sub);
			}
			return size;
		}
		return Math.max(file.length(), MINIMUM_FILE_SIZE);
	}

	/**
	 * If this drive should care about space
	 * @return
	 */
	public boolean caresAboutSpace() {
		return !readOnly && config.storageLimit;
	}
}
