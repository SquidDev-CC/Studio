package squidev.ccstudio.core.filesystem;

import java.io.File;
import java.util.*;

import static squidev.ccstudio.core.utils.StringUtils.*;

/**
 * Stores a filepath
 */
public class FilePath implements Comparable<FilePath> {
	/**
	 * The character that is used to separate directories
	 */
	public static final String DIRECTORY_SEPARATOR = "/";

	/**
	 * Characters that should not appear in a path
	 */
	public static final HashSet<Character> SPECIAL_CHARS =  new HashSet<Character>(Arrays.asList('"', ':', '<', '>', '?', '|'));

	/**
	 * The 'path' to the parent directory
	 */
	public static final String PARENT_DIRECTORY = "..";

	/**
	 * The path to use
	 */
	private String _Path;

	// Properties
	//-----------------------------------------------------------------------

	/**
	 * getMount the path
	 * @return The current path or null if empty
	 *
	 * @see #setPath(String)
	 * @see #_Path
	 */
	public String getPath() {
		return _Path==null ? "" : _Path;
	}

	/**
	 * Set the path
	 * @param value The value to set the path to
	 *
	 * @see #getPath()
	 * @see #_Path
	 */
	public void setPath(String value) {
		_Path = strip(
				value.replace(File.separator, DIRECTORY_SEPARATOR)
						// Don't know if I want to do this:
						.replace(DIRECTORY_SEPARATOR + DIRECTORY_SEPARATOR, DIRECTORY_SEPARATOR),

				//Remove trailing or leading slashes
				DIRECTORY_SEPARATOR
		);
	}

	/**
	 * Is this path the root path
	 * @return
	 */
	public boolean isRoot() {
		return getPath().length() == 0;
	}

	/**
	 * getMount the top element of the path
	 * @return
	 */
	public String getEntityName() {
		if (isRoot()) return null;

		String name = getPath();
		int endOfName = name.length();
		int startOfName = name.lastIndexOf(DIRECTORY_SEPARATOR, endOfName - 1) + 1;
		return name.substring(startOfName, endOfName);
	}

	/**
	 * getMount the parent path of this element
	 * @return
	 *
	 * @throws java.lang.IllegalStateException
	 */
	public FilePath getParentPath() {
		if (isRoot()) throw new IllegalStateException("There is no parent of root.");
		String parentPath = getPath();

		int lookaheadCount = parentPath.length();
		int index = parentPath.lastIndexOf(DIRECTORY_SEPARATOR, lookaheadCount - 1);

		return new FilePath(parentPath.substring(0, index));
	}

	/**
	 * Construct a new file path with several components
	 * @param paths A series of paths to use
	 */
	public FilePath(String... paths) {
		setPath(join(paths, DIRECTORY_SEPARATOR));
	}

	// Modification
	//-----------------------------------------------------------------------
	/**
	 * Removes "../" and non printable characters without wildcards
	 * @return The prettified path
	 *
	 * @see #sanitise(boolean)
	 * @see #sanitise()
	 * @see #prettify(boolean)
	 */
	public FilePath prettify()
	{
		return prettify(false);
	}

	/**
	 * Removes "../" and non printable characters
	 * @param allowWildcards Allow wildcards such as "*"
	 * @return The prettified path
	 *
	 * @see #sanitise(boolean)
	 * @see #sanitise()
	 * @see #prettify()
	 */
	public FilePath prettify(boolean allowWildcards) {
		String path = getPath();

		StringBuilder cleanName = new StringBuilder();
		for(int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);

			// Force c to be above ' ', not include special characters and if @{allowWildcards} is false then remove *
			if (c >= ' ' && !SPECIAL_CHARS.contains(c) && (allowWildcards || c != '*')) cleanName.append(c);
		}
		path = cleanName.toString();

		// If it contains .. then we want to remove them
		if(path.contains(PARENT_DIRECTORY)) {
			String[] parts = path.split(DIRECTORY_SEPARATOR);

			Stack<String> outputParts = new Stack<String>();
			for (String Part : parts) {
				if (Part.length() != 0 && !Part.equals(".")) {
					if (Part.equals(PARENT_DIRECTORY)) {
						if (!outputParts.empty()) {
							String Top = outputParts.peek();
							if (Top.equals(PARENT_DIRECTORY)) { // Then push ".."
								outputParts.push(PARENT_DIRECTORY);
							} else { // Then remove the top element
								outputParts.pop();
							}
						} else {
							outputParts.push(PARENT_DIRECTORY);
						}
					} else if (Part.length() >= 255) { // Limit to 255 characters
						outputParts.push(Part.substring(0, 255));
					} else {
						outputParts.push(Part);
					}
				}
			}

			cleanName = new StringBuilder();
			Iterator iterator = outputParts.iterator();

			while (iterator.hasNext()) {
				String part = (String)iterator.next();
				cleanName.append(part);
				if (iterator.hasNext()) {
					cleanName.append(DIRECTORY_SEPARATOR);
				}
			}

			path = cleanName.toString();
		}

		return new FilePath(path);
	}

	/**
	 * Simplifies path and removes "../"s
	 * @return Sanitised path
	 *
	 * @see #sanitise(boolean)
	 * @see #prettify(boolean)
	 * @see #prettify()
	 */
	public FilePath sanitise() {
		return sanitise(false);
	}

	/**
	 * Simplifies path and removes "../"s
	 * @param allowWildcards Allow wildcards (*)
	 * @return Sanitised path
	 *
	 * @see #sanitise()
	 * @see #prettify(boolean)
	 * @see #prettify()
	 */
	public FilePath sanitise(boolean allowWildcards) {
		FilePath newPath = prettify(allowWildcards);
		return new FilePath(newPath.getPath().replace(PARENT_DIRECTORY, ""));
	}

	/**
	 * append a string onto this path
	 * @param RelativePath The path to append
	 * @return The new path
	 */
	public FilePath appendPath(String RelativePath) {
		return new FilePath(getPath(), RelativePath);
	}

	/**
	 * append a {@see FilePath} onto this path
	 * @param RelativePath The path to append
	 * @return The new path
	 */
	public FilePath appendPath(FilePath RelativePath) {
		return new FilePath(getPath(), RelativePath.getPath());
	}

	// Parent/Children
	//-----------------------------------------------------------------------

	/**
	 * Checks if the path is the parent of another path
	 * @param child The (possible) child path to check
	 * @return {@code true} If this is the parent
	 *
	 * @see #isChildOf(FilePath)
	 */
	public boolean isParentOf(FilePath child) {
		String path = getPath();
		String childPath = child.getPath();

		return path.length() != childPath.length() && childPath.startsWith(path);
	}

	/**
	 * Checks if the path is aa child of another path
	 * @param parent The (possible) parent path to check
	 * @return {@code true} If this is a child
	 *
	 * @see #isParentOf(FilePath)
	 */
	public boolean isChildOf(FilePath parent)
	{
		return parent.isParentOf(this);
	}

	/**
	 * Remove the parent of a path (foo/bar/baz , foo) => bar/baz
	 * @param parent The parent path
	 * @return The resulting path
	 *
	 * @throws java.lang.IllegalArgumentException When parent is not a parent
	 */
	public FilePath removeParent(FilePath parent) {
		String path = getPath();
		String parentPath = parent.getPath();

		if (parentPath.isEmpty()) return clone();
		if (!path.startsWith(parentPath)) throw new IllegalArgumentException("The specified path is not a parent of this path.");
		return new FilePath(path.substring(parentPath.length() + 1));
	}

	/**
	 * Remove the child of a path (foo/bar/baz, baz) => foo/bar
	 * @param child The child path to remove
	 * @return The resulting path
	 *
	 * @throws java.lang.IllegalArgumentException When child is not a child
	 */
	public FilePath removeChild(FilePath child) {
		String path = getPath();
		String childPath = child.getPath();

		if (!path.endsWith(childPath)) throw new IllegalArgumentException("The specified path is not a child of this path.");

		return new FilePath(path.substring(0, path.length() - childPath.length() + 1));
	}

	// General methods
	//-----------------------------------------------------------------------

	@Override
	public int compareTo(FilePath s) {
		return getPath().compareTo(s.getPath());
	}

	@Override
	public String toString() {
		return getPath();
	}

	public FilePath clone() { return new FilePath(getPath()); }

	public boolean equals(FilePath other) {
		return getPath().equals(other.getPath());
	}

	public boolean equals(Object other) {
		return other instanceof FilePath && equals((FilePath) other);
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}


	// Extensions
	//-----------------------------------------------------------------------
	/*
		public string GetExtension()
		{
			string Name = EntityName;
			int ExtensionIndex = Name.LastIndexOf('.');
			if (ExtensionIndex < 0) return "";
			return Name.Substring(ExtensionIndex);
		}

		public FilePath ChangeExtension(string Extension)
		{
			string Name = EntityName;
			int ExtensionIndex = Name.LastIndexOf('.');
			if (ExtensionIndex >= 0) return ParentPath.AppendPath(Name.Substring(0, ExtensionIndex) + Extension);
			return new FilePath(Path + Extension);
		}

		public string[] GetDirectorySegments()
		{
			FilePath Path = this;
			LinkedList<string> Segments = new LinkedList<string>();
			while(!Path.IsRoot)
			{
				Segments.AddFirst(Path.EntityName);
				Path = Path.ParentPath;
			}
			return Segments.ToArray();
		}
	*/
}
