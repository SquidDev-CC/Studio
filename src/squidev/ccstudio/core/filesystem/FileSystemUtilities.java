package squidev.ccstudio.core.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper functions for File systems
 */
public class FileSystemUtilities {
	/**
	 *
	 * @param mount
	 * @param source
	 * @param destination
	 *
	 * @throws java.io.IOException When reading/writing cannot occur
	 * @throws java.lang.IllegalArgumentException When copying a directory inside itself
	 */
	public synchronized static void copyRecursive(IMount mount, FilePath source, FilePath destination)
		throws IOException
	{
		if (source == destination || source.isParentOf(destination)) throw new IllegalArgumentException("Cannot copy a directory inside itself");
		if (!mount.exists(source)) return;

		if (mount.isDirectory(source))
		{
			if(!mount.isDirectory(destination)) mount.makeDirectory(destination);

			for (FilePath Child : mount.list(source))
			{
				copyRecursive(mount, source.appendPath(Child), destination.appendPath(Child));
			}
		}
		else
		{
			InputStream reader = null;
			OutputStream writer = null;

			try {
				reader = mount.read(source);
				writer = mount.write(source);

				byte[] buffer = new byte[1024];
				while (true) {
					int bytesRead = reader.read(buffer);
					if (bytesRead < 0) break;
					writer.write(buffer, 0, bytesRead);
				}
			} finally {
				// Because Java
				if(reader != null) {
					try {
						reader.close();
					} catch(IOException e) { }
				}

				if(writer != null) {
					try {
						writer.close();
					} catch (IOException e) { }
				}
			}
		}
	}
}
