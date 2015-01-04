/*******************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2;

import junit.framework.TestSuite;

import org.luaj.vm2.lua2java.Lua2Java;
import org.luaj.vm2.luajc.LuaJC;

/**
 * Compatibility tests for the Luaj VM
 * 
 * Results are compared for exact match with 
 * the installed C-based lua environment. 
 */
public class CompatibiltyTest extends TestSuite {

	private static final String dir = "";
	
	abstract protected static class CompatibiltyTestSuite extends ScriptDrivenTest {	
		LuaValue savedStringMetatable;
		protected CompatibiltyTestSuite(PlatformType platform) {
			super(platform,dir);
		}
		
		protected void setUp() throws Exception {
			savedStringMetatable = LuaString.s_metatable;
			super.setUp();
		}

		protected void tearDown() throws Exception {
			super.tearDown();
			LuaNil.s_metatable = null;
			LuaBoolean.s_metatable = null;
			LuaNumber.s_metatable = null;
			LuaFunction.s_metatable = null;
			LuaThread.s_metatable = null;
			LuaString.s_metatable = savedStringMetatable;
		}

		public void testBaseLib()       { runTest("baselib");   }
		public void testCoroutineLib()  { runTest("coroutinelib"); }	
		public void testDebugLib()      { runTest("debuglib"); }	
		public void testErrors()        { runTest("errors"); }	
		public void testFunctions()     { runTest("functions"); }	
		public void testIoLib()         { runTest("iolib");     }
		public void testManyUpvals()    { runTest("manyupvals"); }
		public void testMathLib()       { runTest("mathlib"); }
		public void testMetatags()      { runTest("metatags"); }
		public void testOsLib()         { runTest("oslib"); }
		public void testStringLib()     { runTest("stringlib"); }
		public void testTableLib()      { runTest("tablelib"); }
		public void testTailcalls()     { runTest("tailcalls"); }
		public void testUpvalues()      { runTest("upvalues"); }
		public void testVm()            { runTest("vm"); }
	}


	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Compatibility Tests");
		suite.addTest( new TestSuite( JseCompatibilityTest.class,   "JSE Tests" ) );
		suite.addTest( new TestSuite( JmeCompatibilityTest.class,   "JME Tests" ) );
		suite.addTest( new TestSuite( LuaJCTest.class,        "JSE Bytecode Tests" ) );
		suite.addTest( new TestSuite( Lua2JavaTest.class,   		"Lua2Java Tests" ) );
		return suite;
	}
	
	public static class Lua2JavaTest extends CompatibiltyTestSuite {
		public Lua2JavaTest() {
			super(ScriptDrivenTest.PlatformType.LUA2JAVA);
		}
		protected void setUp() throws Exception {
			super.setUp();
			System.setProperty("JME", "false");
			Lua2Java.install();
		}
	}

	public static class JmeCompatibilityTest extends CompatibiltyTestSuite {
		public JmeCompatibilityTest() {
			super(ScriptDrivenTest.PlatformType.JME);
		}
		protected void setUp() throws Exception {
			System.setProperty("JME", "true");
			super.setUp();
		}
	}
	public static class JseCompatibilityTest extends CompatibiltyTestSuite {
		public JseCompatibilityTest() {
			super(ScriptDrivenTest.PlatformType.JSE);
		}
		protected void setUp() throws Exception {
			super.setUp();
			System.setProperty("JME", "false");
		}
	}
	public static class LuaJCTest extends CompatibiltyTestSuite {
		public LuaJCTest() {
			super(ScriptDrivenTest.PlatformType.LUAJIT);
		}
		protected void setUp() throws Exception {
			super.setUp();
			System.setProperty("JME", "false");
			LuaJC.install();
		}
		// not supported on this platform - don't test
		public void testDebugLib()      {}	
	}
}
