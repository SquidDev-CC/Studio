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
package org.luaj.luajc;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;

public class TestLuaJC {
	// create the script
	public static String name = "script";
	public static String script =
		"local t = a or nil\n"+
		"local t\n" +
		"b = function()\n"+
		"	return t\n"+
		"end\n"+
		"return t\n"+
		"";

	public static void main(String[] args) throws Exception {
		System.out.println(script);
		try {

			// create an environment to run in
			LuaTable _G = JsePlatform.standardGlobals();

			// compile into a chunk, or load as a class
			LuaValue chunk;
			if ( ! (args.length>0 && args[0].equals("nocompile")) ) {
				InputStream is =  new ByteArrayInputStream( script.getBytes() );
				chunk = LuaJC.getInstance().load(is, "script", _G);
			} else {
				chunk = (LuaValue) Class.forName("script").newInstance();
			}
			chunk.setfenv(_G);

			// call with arguments
			LuaValue[] vargs = new LuaValue[args.length];
			for ( int i=0; i<args.length; i++ )
				vargs[i] = LuaValue.valueOf(args[i]);
			Varargs cargs = LuaValue.varargsOf(vargs);
			Varargs v = chunk.invoke(cargs);

			// print the result
			for ( int i=1; i<=v.narg(); i++ )
				System.out.println("result["+i+"]: "+v.arg(i));
		} catch ( Throwable e ) {
			e.printStackTrace();
			saveClasses();
		}
	}

	private static void saveClasses() throws Exception {
        // create the chunk
		String chunkname = "script";
		String filename = "script";
		String destdir = ".";

		InputStream is = new ByteArrayInputStream( script.getBytes() );
		Hashtable t = LuaJC.getInstance().compileAll(is, chunkname, filename);

        // write out the chunk
    	for ( Enumeration e = t.keys(); e.hasMoreElements(); ) {
    		String key = (String) e.nextElement();
    		byte[] bytes = (byte[]) t.get(key);
    		String destpath = (destdir!=null? destdir+"/": "") + key + ".class";
    		System.out.println(
						"chunk "+chunkname+
						" from "+filename+
						" written to "+destpath
						+" length="+bytes.length+" bytes");
        	FileOutputStream fos = new FileOutputStream( destpath );
        	fos.write( bytes );
        	fos.close();
        }

	}

}
