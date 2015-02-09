package squidev.ccstudio.computer.api;

import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;
import dan200.computercraft.core.filesystem.IMountedFileBinary;
import dan200.computercraft.core.filesystem.IMountedFileNormal;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squidev.ccstudio.computer.Computer;
import squidev.ccstudio.core.apis.wrapper.LuaAPI;
import squidev.ccstudio.core.apis.wrapper.LuaFunction;

import java.io.IOException;

@LuaAPI("fs")
public class FileSystemAPI {
	protected final Computer computer;
	protected FileSystem fileSystem;

	public FileSystemAPI(Computer computer) {
		this.computer = computer;
	}

	public FileSystem getFileSystem() {
		FileSystem fs = fileSystem;
		return fs == null ? (fileSystem = computer.getFileSystem()) : fs;
	}

	@LuaFunction
	public LuaTable list(String path) {
		try {
			String[] elements = getFileSystem().list(path);

			int length = elements.length;
			LuaTable result = new LuaTable(length, 0);

			for (int i = 0; i < length; i++) {
				result.insert(i + 1, LuaValue.valueOf(elements[i]));
			}

			return result;
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public String combine(String a, String b) {
		return getFileSystem().combine(a, b);
	}

	@LuaFunction
	public String getName(String path) {
		return FileSystem.getName(path);
	}

	@LuaFunction
	public double getSize(String path) {
		try {
			return getFileSystem().getSize(path);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public boolean exists(String path) {
		try {
			return getFileSystem().exists(path);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public boolean isDir(String path) {
		try {
			return getFileSystem().isDir(path);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public boolean isReadOnly(String path) {
		try {
			return getFileSystem().isReadOnly(path);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public void makeDir(String path) {
		try {
			getFileSystem().makeDir(path);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public void move(String from, String to) {
		try {
			getFileSystem().move(from, to);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public void copy(String from, String to) {
		try {
			getFileSystem().copy(from, to);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public void delete(String path) {
		try {
			getFileSystem().delete(path);
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public LuaValue open(String path, String mode) {
		try {
			Object toCreate;
			switch (mode) {
				case "r":
					toCreate = new NormalFileReader(getFileSystem().openForRead(path));
					break;
				case "w":
					toCreate = new NormalFileWriter(getFileSystem().openForWrite(path, false));
					break;
				case "a":
					toCreate = new NormalFileWriter(getFileSystem().openForWrite(path, true));
					break;

				case "rb":
					toCreate = new BinaryFileReader(getFileSystem().openForBinaryRead(path));
					break;
				case "wb":
					toCreate = new BinaryFileReader(getFileSystem().openForBinaryWrite(path, false));
					break;
				case "ab":
					toCreate = new BinaryFileReader(getFileSystem().openForBinaryWrite(path, true));
					break;

				default:
					throw new LuaError("Unsupported mode");
			}

			return computer.createLuaObject(toCreate).getTable();
		} catch (FileSystemException e) {
			return null;
		}
	}

	@LuaFunction
	public String getDrive(String path) {
		try {
			FileSystem fs = getFileSystem();
			if (fs.exists(path)) return fs.getMountLabel(path);
			return null;
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public LuaValue getFreeSpace(String path) {
		try {
			long size = getFileSystem().getFreeSpace(path);
			return size >= 0 ? LuaValue.valueOf(size) : LuaValue.valueOf("unlimited");
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public LuaTable find(String pattern) {
		try {
			String[] elements = getFileSystem().find(pattern);

			int length = elements.length;
			LuaTable result = new LuaTable(length, 0);

			for (int i = 0; i < length; i++) {
				result.insert(i + 1, LuaValue.valueOf(elements[i]));
			}

			return result;
		} catch (FileSystemException e) {
			throw new LuaError(e.getMessage());
		}
	}

	@LuaFunction
	public String getDir(String path) {
		return FileSystem.getDirectory(path);
	}

	public static class NormalFileReader {
		protected final IMountedFileNormal file;

		public NormalFileReader(IMountedFileNormal file) {
			this.file = file;
		}

		@LuaFunction
		public String readLine() {
			try {
				return file.readLine();
			} catch (IOException e) {
				return null;
			}
		}

		@LuaFunction
		public String readAll() {
			try {
				StringBuilder builder = new StringBuilder("");
				IMountedFileNormal reader = file;
				String line = reader.readLine();

				while (line != null) {
					builder.append(line);
					line = reader.readLine();
					if (line != null) {
						builder.append("\n");
					}
				}
				return builder.toString();
			} catch (IOException e) {
				return null;
			}
		}

		@LuaFunction
		public void close() {
			try {
				file.close();
			} catch (IOException ignored) {
			}
		}
	}

	public static class NormalFileWriter {
		protected final IMountedFileNormal file;

		public NormalFileWriter(IMountedFileNormal file) {
			this.file = file;
		}

		@LuaFunction
		public void write(Varargs args) {
			try {
				if (args.narg() > 0) {
					String contents = args.arg1().toString();
					file.write(contents, 0, contents.length(), false);
				}
			} catch (IOException ignored) {
			}
		}

		@LuaFunction
		public void writeLine(Varargs args) {
			try {
				if (args.narg() > 0) {
					String contents = args.arg1().toString();
					file.write(contents, 0, contents.length(), true);
				}
			} catch (IOException ignored) {
			}
		}

		@LuaFunction
		public void close() {
			try {
				file.close();
			} catch (IOException ignored) {
			}
		}

		@LuaFunction
		public void flush() {
			try {
				file.close();
			} catch (IOException ignored) {
			}
		}
	}

	public static class BinaryFileReader {
		protected final IMountedFileBinary file;

		public BinaryFileReader(IMountedFileBinary file) {
			this.file = file;
		}

		@LuaFunction
		public LuaValue read() {
			try {
				return LuaValue.valueOf(file.read());
			} catch (IOException ignored) {
				return LuaValue.NONE;
			}
		}

		@LuaFunction
		public void close() {
			try {
				file.close();
			} catch (IOException ignored) {
			}
		}
	}

	public static class BinaryFileWriter {
		protected final IMountedFileBinary file;

		public BinaryFileWriter(IMountedFileBinary file) {
			this.file = file;
		}

		@LuaFunction
		public void write(double value) {
			try {
				file.write((int) value);
			} catch (IOException ignored) {
			}
		}

		@LuaFunction
		public void flush() {
			try {
				file.flush();
			} catch (IOException ignored) {
			}
		}

		@LuaFunction
		public void close() {
			try {
				file.close();
			} catch (IOException ignored) {
			}
		}
	}
}
