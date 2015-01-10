package squidev.ccstudio.core.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Holds a filesystem mount
 */
public interface IMount {
	public static final int MINIMUM_FILE_SIZE = 512;
	public static final int MAX_SPACE = 1000000; // 1 Megabyte (Sort of)

	public static final String ACCESS_DENIED = "Access denied";

	String getLabel(FilePath filePath);

	boolean isReadOnly(FilePath filePath);

	boolean exists(FilePath filePath);
	boolean isDirectory(FilePath filePath);

	Iterable<FilePath> list(FilePath filePath) throws IOException;

	long getSize(FilePath filePath) throws IOException;
	long getRemainingSpace(FilePath filePath);

	InputStream read(FilePath FileP)throws IOException;
	OutputStream write(FilePath FileP)throws IOException;
	OutputStream append(FilePath filePath)throws IOException;

	void makeDirectory(FilePath filePath) throws IOException;
	void delete(FilePath filePath) throws IOException;
}
