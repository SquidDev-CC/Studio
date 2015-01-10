package squidev.ccstudio.core.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * A mount that holds several child mounts
 */
public class ComboMount implements IMount {
	public Map<FilePath,IMount> childMounts;

	public ComboMount(Map<FilePath, IMount> mounts) {
		this();
		childMounts.putAll(mounts);
	}
	public ComboMount() {
		childMounts = new TreeMap<FilePath, IMount>(Collections.reverseOrder());
	}

	protected Map.Entry<FilePath,IMount> getMount(FilePath path) throws IOException {
		for(Map.Entry<FilePath, IMount> subPath : childMounts.entrySet()) {
			if(subPath.getKey().equals(path) || subPath.getKey().isParentOf(path)) {
				return subPath;
			}
		}
		throw new IOException("No such file");
	}

	public String getLabel(FilePath filePath) {
		try {
			Map.Entry<FilePath, IMount> sub = getMount(filePath);
			return sub.getValue().getLabel(filePath.removeParent(sub.getKey()));
		} catch(IOException e) {
			return null;
		}
	}

	@Override
	public boolean isReadOnly(FilePath filePath) {
		try {
			Map.Entry<FilePath, IMount> sub = getMount(filePath);
			return sub.getValue().isReadOnly(filePath.removeParent(sub.getKey()));
		} catch(IOException e) {
			return false;
		}
	}

	@Override
	public boolean exists(FilePath filePath) {
		try {
			Map.Entry<FilePath, IMount> sub = getMount(filePath);
			return sub.getValue().exists(filePath.removeParent(sub.getKey()));
		} catch(IOException e) {
			return false;
		}
	}

	@Override
	public boolean isDirectory(FilePath filePath) {
		try {
			Map.Entry<FilePath, IMount> sub = getMount(filePath);
			return sub.getValue().isDirectory(filePath.removeParent(sub.getKey()));
		} catch(IOException e) {
			return false;
		}
	}

	@Override
	public Iterable<FilePath> list(FilePath filePath) throws IOException {
		SortedSet<FilePath> paths = new TreeSet<FilePath>();

		for(Map.Entry<FilePath, IMount> subPath : childMounts.entrySet()) {
			if(subPath.getKey().equals(filePath) || subPath.getKey().isParentOf(filePath)) {
				for(FilePath item : subPath.getValue().list(filePath.removeParent(subPath.getKey()))) {
					paths.add(item);
				}
			}
		}

		/*
		for (KeyValuePair<FilePath, IMount> Child in Mounts.Where(Pair => Pair.Key.IsChildOf(Path)))
		{
			FilePath ChildPath = Child.Key;
			while (true)
			{
				FilePath Parent = ChildPath.ParentPath;

				if (Parent == Path)
				{
					break;
				}

				ChildPath = Parent;
			}
			paths.Add(ChildPath.RemoveParent(Path));
		}*/

		return paths;
	}

	@Override
	public long getSize(FilePath filePath) throws IOException {
		Map.Entry<FilePath, IMount> subPath = getMount(filePath);
		return subPath.getValue().getSize(filePath.removeParent(subPath.getKey()));
	}

	@Override
	public long getRemainingSpace(FilePath filePath) {
			try {
				Map.Entry<FilePath, IMount> subPath = getMount(filePath);
				return subPath.getValue().getSize(filePath.removeParent(subPath.getKey()));
			} catch (IOException e) {
				return 0L;
			}
	}

	@Override
	public InputStream read(FilePath FileP) throws IOException {
		return null;
	}

	@Override
	public OutputStream write(FilePath FileP) throws IOException {
		return null;
	}

	@Override
	public OutputStream append(FilePath filePath) throws IOException {
		return null;
	}

	@Override
	public void makeDirectory(FilePath filePath) throws IOException {
		try {
			Map.Entry<FilePath, IMount> subPath = getMount(filePath);
			subPath.getValue().makeDirectory(filePath.removeParent(subPath.getKey()));
		} catch (IOException e) {
			throw new IOException(ACCESS_DENIED);
		}
	}

	@Override
	public void delete(FilePath filePath) throws IOException {
		try {
			Map.Entry<FilePath, IMount> subPath = getMount(filePath);
			subPath.getValue().delete(filePath.removeParent(subPath.getKey()));
		} catch (IOException e) {
			throw new IOException(ACCESS_DENIED);
		}
	}
}
