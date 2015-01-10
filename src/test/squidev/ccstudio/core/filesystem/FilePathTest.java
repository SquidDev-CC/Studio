package squidev.ccstudio.core.filesystem;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FilePathTest {
	protected void assertPath(String expected, FilePath actual) {
		assertEquals(expected, actual.getPath());
	}

	@Test
	public void testSetPath() throws Exception {
		// Test converting slashes
		assertPath("thing/ok", new FilePath("thing/ok"));
		assertPath("thing/ok", new FilePath("thing\\ok"));

		// Test repeated slashes
		assertPath("thing/ok", new FilePath("thing//ok"));
		assertPath("thing/ok", new FilePath("thing\\\\ok"));
		assertPath("thing/ok", new FilePath("thing///ok"));
		assertPath("thing/ok", new FilePath("thing\\\\\\ok"));

		assertPath("thing/ok", new FilePath("/thing/ok"));
		assertPath("thing/ok", new FilePath("thing/ok/"));
		assertPath("thing/ok", new FilePath("/thing/ok/"));
	}

	@Test
	public void testIsRoot() throws Exception {
		assertTrue(new FilePath("").isRoot());
		assertTrue(new FilePath("/").isRoot());
		assertTrue(new FilePath("//").isRoot());

		assertFalse(new FilePath("/sub").isRoot());
		assertFalse(new FilePath("sub").isRoot());
		assertFalse(new FilePath("//sub").isRoot());
	}

	@Test
	public void testGetEntityName() throws Exception {
		assertEquals("", new FilePath("").getEntityName());
		assertEquals("path", new FilePath("path").getEntityName());
		assertEquals("path", new FilePath("sub/path/").getEntityName());
	}

	@Test
	public void testGetParentPath() throws Exception {
		try {
			new FilePath("").getParentPath();
			assertTrue("Cannot get root's parent", false);
		} catch(Exception e) {
			assertEquals("There is no parent of root.", e.getMessage());
		}

		assertPath("", new FilePath("thing").getParentPath());
		assertPath("thing", new FilePath("thing/sub").getParentPath());
		assertPath("thing/sub", new FilePath("thing/sub/another").getParentPath());
	}

	@Test
	public void testPrettify() throws Exception {
		// Handle illegal characters
		assertPath("test", new FilePath("test\"").prettify());
		assertPath("test", new FilePath("test:\"<").prettify());

		// Handle path combinations
		assertPath("test", new FilePath("test/another/../").prettify());
		assertPath("test", new FilePath("another/../test").prettify());

		assertPath("../../test", new FilePath("another/../../../test").prettify());
		assertPath("....", new FilePath("....").prettify());

		// Handle wildcards
		assertPath("test.lua", new FilePath("test*.lua").prettify());
		assertPath("test*.lua", new FilePath("test*.lua").prettify(true));
	}

	@Test
	public void testSanitise() throws Exception {
		assertPath("test", new FilePath("another/../../../test").sanitise());
		assertPath("", new FilePath("....").sanitise());
	}

	@Test
	public void testAppendPath() throws Exception {
		// Not really sure what to test here
		assertPath("test/things", new FilePath("test").appendPath(new FilePath("things")));
	}

	@Test
	public void testIsParentOf() throws Exception {
		assertTrue(new FilePath("test").isParentOf(new FilePath("test/thing")));
		assertTrue(new FilePath("test").isParentOf(new FilePath("test/thing/another")));
		assertTrue(new FilePath("test/thing").isParentOf(new FilePath("test/thing/another")));

		assertFalse(new FilePath("thing").isParentOf(new FilePath("thing")));

		assertFalse(new FilePath("another").isParentOf(new FilePath("thing/another")));
		assertFalse(new FilePath("another").isParentOf(new FilePath("anotherThing")));
	}

	@Test
	public void testRemoveParent() throws Exception {
		assertPath("another", new FilePath("test/another").removeParent(new FilePath("test")));
		assertPath("another", new FilePath("test/sub/another").removeParent(new FilePath("test/sub")));
		assertPath("sub/another", new FilePath("test/sub/another").removeParent(new FilePath("test")));
	}

	@Test
	public void testRemoveChild() throws Exception {
		// TODO: Write a test for this
	}
}
