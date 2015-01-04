package squidev.ccstudio.core.filesystem;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Holds a filesystem mount
 */
public interface IMount {
	public static final int MINIMUM_FILE_SIZE = 512;

	String getLabel(FilePath filePath);

	boolean isReadOnly(FilePath filePath);

	boolean exists(FilePath filePath);
	boolean isDirectory(FilePath filePath);

	Iterable<FilePath> list(FilePath filePath);

	long getSize(FilePath filePath);
	long getRemainingSpace();

	InputStream read(FilePath FileP);
	OutputStream write(FilePath FileP);
	OutputStream append(FilePath filePath);

	void makeDirectory(FilePath filePath);
	void delete(FilePath filePath);
}
