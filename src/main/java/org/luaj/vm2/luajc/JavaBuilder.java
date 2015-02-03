/*******************************************************************************
 * Copyright (c) 2010 Luaj.org. All rights reserved.
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

package org.luaj.vm2.luajc;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import squidev.ccstudio.core.Config;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static squidev.ccstudio.core.asm.AsmUtils.TinyMethod;
import static squidev.ccstudio.core.asm.AsmUtils.constantOpcode;

public class JavaBuilder {

	private static final String TYPE_LOCALUPVALUE = org.objectweb.asm.Type.getInternalName(LuaValue[].class);
	private static final String TYPE_LUAVALUE = org.objectweb.asm.Type.getInternalName(LuaValue.class);

	protected static class FunctionType {
		public final String signature;
		public final String methodName;
		public final String className;

		public FunctionType(String name, String invokeName, String invokeSignature) {
			className = name;
			methodName = invokeName;
			signature = invokeSignature;
		}

		public FunctionType(Class classObj, String invokeName, Class... args) {
			this(
					org.objectweb.asm.Type.getDescriptor(classObj),
					invokeName, getSignature(classObj, invokeName, args)
			);
		}

		private static String getSignature(Class classObj, String invokeName, Class... args) {
			try {
				return org.objectweb.asm.Type.getMethodDescriptor(classObj.getMethod(invokeName, args));
			} catch(Exception e) { }
			return "()V";
		}
	}

	// Manage super classes
	private static FunctionType[] SUPER_TYPES = {
			new FunctionType(ZeroArgFunction.class, "call"),
			new FunctionType(OneArgFunction.class, "call", LuaValue.class),
			new FunctionType(TwoArgFunction.class, "call", LuaValue.class, LuaValue.class),
			new FunctionType(ThreeArgFunction.class, "call", LuaValue.class, LuaValue.class, LuaValue.class),
			new FunctionType(VarArgFunction.class, "onInvoke", Varargs.class),
	};

	// Table functions
	private static final TinyMethod METHOD_TABLEOF = TinyMethod.tryConstruct(LuaValue.class, "tableOf", Varargs.class, int.class);
	private static final TinyMethod METHOD_TABLEOF_DIMS = TinyMethod.tryConstruct(LuaValue.class, "tableOf", int.class, int.class);
	private static final TinyMethod METHOD_TABLE_GET = TinyMethod.tryConstruct(LuaValue.class, "get", LuaValue.class);
	private static final TinyMethod METHOD_TABLE_SET = TinyMethod.tryConstruct(LuaValue.class, "set", LuaValue.class);

	// Strings
	private static final TinyMethod METHOD_STRING_CONCAT = TinyMethod.tryConstruct(LuaValue.class, "concat", LuaValue.class);
	private static final TinyMethod METHOD_BUFFER_CONCAT = TinyMethod.tryConstruct(LuaValue.class, "concat", Buffer.class);

	// Varargs
	private static final TinyMethod METHOD_VARARGS_ARG1 = TinyMethod.tryConstruct(Varargs.class, "arg1");
	private static final TinyMethod METHOD_VARARGS_ARG = TinyMethod.tryConstruct(Varargs.class, "arg", int.class);
	private static final TinyMethod METHOD_VARARGS_SUBARGS = TinyMethod.tryConstruct(Varargs.class, "subargs", int.class);

	// Varargs factory
	private static final TinyMethod METHOD_VARARGS_ONE = TinyMethod.tryConstruct(LuaValue.class, "varargsOf", LuaValue.class, Varargs.class);
	private static final TinyMethod METHOD_VARARGS_TWO = TinyMethod.tryConstruct(LuaValue.class, "varargsOf", LuaValue.class, LuaValue.class, Varargs.class);
	private static final TinyMethod METHOD_VARARGS_MANY = TinyMethod.tryConstruct(LuaValue.class, "varargsOf", LuaValue[].class);
	private static final TinyMethod METHOD_VARARGS_MANY_VAR = TinyMethod.tryConstruct(LuaValue.class, "varargsOf", LuaValue[].class, Varargs.class);

	// Type conversion
	private static final TinyMethod METHOD_VALUE_TO_BOOL = TinyMethod.tryConstruct(LuaValue.class, "toboolean");
	private static final TinyMethod METHOD_BUFFER_TO_STR = TinyMethod.tryConstruct(Buffer.class, "tostring");
	private static final TinyMethod METHOD_VALUE_TO_BUFFER = TinyMethod.tryConstruct(LuaValue.class, "buffer");
	private static final TinyMethod METHOD_BUFFER_TO_VALUE = TinyMethod.tryConstruct(Buffer.class, "value");

	// Booleans
	private static final TinyMethod METHOD_TESTFOR_B = TinyMethod.tryConstruct(LuaValue.class, "testfor_b", LuaValue.class, LuaValue.class);
	private static final TinyMethod METHOD_IS_NIL = TinyMethod.tryConstruct(LuaValue.class, "isnil");

	// Calling
	// Normal
	private static final TinyMethod METHOD_CALL_NONE = TinyMethod.tryConstruct(LuaValue.class, "call");
	private static final TinyMethod METHOD_CALL_ONE = TinyMethod.tryConstruct(LuaValue.class, "call", LuaValue.class);
	private static final TinyMethod METHOD_CALL_TWO = TinyMethod.tryConstruct(LuaValue.class, "call", LuaValue.class, LuaValue.class);
	private static final TinyMethod METHOD_CALL_THREE = TinyMethod.tryConstruct(LuaValue.class, "call", LuaValue.class, LuaValue.class, LuaValue.class);

	// Tail call
	private static final TinyMethod METHOD_TAILCALL = TinyMethod.tryConstruct(LuaValue.class, "tailcallOf", LuaValue.class, Varargs.class);

	// Invoke (because that is different to call?) Well, it is but really silly
	private static final TinyMethod METHOD_INVOKE_VAR = TinyMethod.tryConstruct(LuaValue.class, "invoke", Varargs.class);
	private static final TinyMethod METHOD_INVOKE_TWO = TinyMethod.tryConstruct(LuaValue.class, "invoke");
	private static final TinyMethod METHOD_INVOKE_NONE = TinyMethod.tryConstruct(LuaValue.class, "invoke", LuaValue.class, Varargs.class);
	private static final TinyMethod METHOD_INVOKE_THREE = TinyMethod.tryConstruct(LuaValue.class, "invoke", LuaValue.class, LuaValue.class, Varargs.class);

	// ValueOf
	private static final TinyMethod METHOD_VALUEOF_INT = TinyMethod.tryConstruct(LuaValue.class, "valueOf", int.class);
	private static final TinyMethod METHOD_VALUEOF_DOUBLE = TinyMethod.tryConstruct(LuaValue.class, "valueOf", double.class);
	private static final TinyMethod METHOD_VALUEOF_STRING = TinyMethod.tryConstruct(LuaString.class, "valueOf", String.class);
	private static final TinyMethod METHOD_VALUEOF_CHARARRAY = TinyMethod.tryConstruct(LuaString.class, "valueOf", char[].class);

	// Misc
	private static final TinyMethod METHOD_SETENV = TinyMethod.tryConstruct(LuaValue.class, "setfenv", LuaValue.class);
	private static final TinyMethod METHOD_TO_CHARARRAY = TinyMethod.tryConstruct(String.class, "toCharArray");
	private static final TinyMethod METHOD_RAWSET = TinyMethod.tryConstruct(LuaValue.class, "rawset", int.class, LuaValue.class);
	private static final TinyMethod METHOD_RAWSET_LIST = TinyMethod.tryConstruct(LuaValue.class, "rawsetlist", int.class, LuaValue.class);

	// TODO: Can this be LibFunction? Is there a performance change?
	private final TinyMethod methodCurrentNewUpvalueEmpty;
	private final TinyMethod methodCurrentNewUpvalueNil;
	private final TinyMethod methodCurrentNewUpvalueValue;

	// Varable naming
	private static final String PREFIX_CONSTANT = "k";
	private static final String PREFIX_UPVALUE = "u";
	private static final String PREFIX_PLAIN_SLOT = "s";
	private static final String PREFIX_UPVALUE_SLOT = "a";

	// Basic info
	private final ProtoInfo pi;
	private final Prototype p;
	private final String className;

	/**
	 * Main class writer
	 */
	private final ClassWriter writer;

	/**
	 * The static constructor method
	 */
	private final MethodVisitor init;

	/**
	 * The function invoke
	 */
	private final MethodVisitor main;

	/**
	 * Max number of locals
	 */
	private int maxLocals = 0;

	/**
	 * The local index of the varargs result
	 */
	private int varargsLocal = -1;

	/**
	 * The current lua source location
	 */
	private int pc = 0;

	// the superclass arg count, 0-3 args, 4=varargs
	private int superclassType;
	private static int SUPERTYPE_VARARGS = 4;

	// Storage for goto locations
	private final Label[] branchDestinations;
	private Label currentLabel = null;

	public JavaBuilder(ProtoInfo pi, String className, String filename) {
		this.pi = pi;
		this.p = pi.prototype;
		this.className = className;

		// Create some more functions
		methodCurrentNewUpvalueEmpty = new TinyMethod(className, "newupe", "()" + TYPE_LOCALUPVALUE, true);
		methodCurrentNewUpvalueNil = new TinyMethod(className, "newupn", "()" + TYPE_LOCALUPVALUE, true);
		methodCurrentNewUpvalueValue = new TinyMethod(className, "newupl", "(" + TYPE_LUAVALUE + ")" + TYPE_LOCALUPVALUE, true);

		// what class to inherit from
		superclassType = p.numparams;
		if (p.is_vararg != 0 || superclassType >= SUPERTYPE_VARARGS) {
			superclassType = SUPERTYPE_VARARGS;
		}

		// If we return var args, then must be a var arg function
		for (int i = 0, n = p.code.length; i < n; i++) {
			int inst = p.code[i];
			int o = Lua.GET_OPCODE(inst);
			if ((o == Lua.OP_TAILCALL) || ((o == Lua.OP_RETURN) && (Lua.GETARG_B(inst) < 1 || Lua.GETARG_B(inst) > 2))) {
				superclassType = SUPERTYPE_VARARGS;
				break;
			}
		}

		FunctionType superType = SUPER_TYPES[superclassType];

		// Create class writer
		// TODO: Do I need to compute frames?
		writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

		// Check the name of the class. We have no interfaces and no generics
		writer.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, superType.className, null);

		// Write the filename
		writer.visitSource(filename, null);

		// Create the fields
		for (int i = 0; i < p.nups; i++) {
			boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[i]);
			String type = isReadWrite ? TYPE_LOCALUPVALUE : TYPE_LUAVALUE;
			writer.visitField(0, upvalueName(i), type, null, null);
		}

		// Create the invoke method
		main = writer.visitMethod(ACC_PUBLIC + ACC_FINAL, superType.methodName, superType.signature, null, null);
		main.visitCode();

		init = writer.visitMethod(ACC_STATIC, "<clinit>", "V()", null, null);
		init.visitCode();

		// Initialize the values in the slots
		initializeSlots();

		// initialize branching
		int nc = p.code.length;
		targets = new int[nc];

		// Generate a label for every instruction
		Label[] branchDestinations = this.branchDestinations = new Label[nc];
		for(int i = 0; i < nc; i++) {
			branchDestinations[i] = new Label();
		}
	}

	public void initializeSlots() {
		int slot;
		createUpvalues(-1, 0, p.maxstacksize);

		if (superclassType == SUPERTYPE_VARARGS) {
			for (slot = 0; slot < p.numparams; slot++) {
				if (pi.isInitialValueUsed(slot)) {
					main.visitVarInsn(ALOAD, 1);
					constantOpcode(main, slot + 1);
					METHOD_VARARGS_ARG.inject(main, INVOKEVIRTUAL);
					storeLocal(-1, slot);
				}
			}
			boolean needsArg = ((p.is_vararg & Lua.VARARG_NEEDSARG) != 0);
			if (needsArg) {
				main.visitVarInsn(ALOAD, 1);
				constantOpcode(main, p.numparams + 1);
				METHOD_TABLEOF.inject(main, INVOKESTATIC);
				storeLocal(-1, slot++);
			} else if (p.numparams > 0) {
				main.visitVarInsn(ALOAD, 1);
				constantOpcode(main, p.numparams + 1);
				METHOD_VARARGS_SUBARGS.inject(main, INVOKEVIRTUAL);
				main.visitVarInsn(ASTORE, 1);
			}
		} else {
			// fixed arg function between 0 and 3 arguments
			for (slot = 0; slot < p.numparams; slot++) {
				this.plainSlotVars.put(slot, slot + 1);
				if (pi.isUpvalueCreate(-1, slot)) {
					main.visitVarInsn(ALOAD, 1);
					storeLocal(-1, slot);
				}
			}
		}

		// nil parameters
		for (; slot < p.maxstacksize; slot++) {
			if (pi.isInitialValueUsed(slot)) {
				loadNil();
				storeLocal(-1, slot);
			}
		}
	}

	public byte[] completeClass() {
		// Add class initializer
		init.visitInsn(RETURN);
		init.visitMaxs(0, 0);
		init.visitEnd();

		// Add default constructor
		MethodVisitor construct = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		construct.visitVarInsn(ALOAD, 0);
		construct.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		construct.visitInsn(RETURN);
		construct.visitMaxs(1, 1);
		construct.visitEnd();

		// Gen method
		resolveBranches();
		main.visitMaxs(0, 0);
		main.visitEnd();

		writer.visitEnd();

		// convert to class bytes
		byte[] bytes = writer.toByteArray();
		if (Config.verifySources) {
			CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.out));
		}
		return bytes;
	}

	public void dup() {
		onStartOfLuaInstruction();
		main.visitInsn(DUP);
	}

	public void pop() {
		onStartOfLuaInstruction();
		main.visitInsn(POP);
	}

	public void loadNil() {
		onStartOfLuaInstruction();
		main.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NIL", "Lorg/luaj/vm2/LuaValue;");
	}

	public void loadNone() {
		onStartOfLuaInstruction();

		main.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
	}

	public void loadBoolean(boolean b) {
		onStartOfLuaInstruction();

		String field = (b ? "TRUE" : "FALSE");
		main.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", field, "Lorg/luaj/vm2/LuaBoolean;");
	}

	private Map<Integer, Integer> plainSlotVars = new HashMap<>();
	private Map<Integer, String> javaSlotNames = new HashMap<>();
	private Map<Integer, Integer> upvalueSlotVars = new HashMap<>();

	private int findSlot(int slot, Map<Integer, Integer> map, String prefix) {
		Integer luaSlot = slot;
		if (map.containsKey(luaSlot)) return map.get(luaSlot);

		// This will always be an Upvalue/LuaValue so the slot size is 1 as it is a reference
		int javaSlot = ++maxLocals;
		javaSlotNames.put(javaSlot, prefix + slot); // This should probably be debug only or something
		map.put(luaSlot, javaSlot);
		return javaSlot;
	}

	private int findSlotIndex(int slot, boolean isUpvalue) {
		return isUpvalue ?
				findSlot(slot, upvalueSlotVars, PREFIX_UPVALUE_SLOT) :
				findSlot(slot, plainSlotVars, PREFIX_PLAIN_SLOT);
	}

	public void loadLocal(int pc, int slot) {
		onStartOfLuaInstruction();

		boolean isUpvalue = pi.isUpvalueRefer(pc, slot);
		int index = findSlotIndex(slot, isUpvalue);

		main.visitVarInsn(ALOAD, index);
		if (isUpvalue) {
			main.visitInsn(ICONST_0);
			main.visitInsn(AALOAD);
		}
	}

	public void storeLocal(int pc, int slot) {
		onStartOfLuaInstruction();

		boolean isUpvalue = pi.isUpvalueAssign(pc, slot);
		int index = findSlotIndex(slot, isUpvalue);
		if (isUpvalue) {
			boolean isUpCreate = pi.isUpvalueCreate(pc, slot);
			if (isUpCreate) {
				// If we are creating the upvalue for the first time then we call LibFunction.newupe (but actually call
				// <className>.newupe but I need to check that). The we duplicate the object, so it remains on the stack
				// and store it
				methodCurrentNewUpvalueEmpty.inject(main);
				main.visitInsn(DUP);
				main.visitVarInsn(ASTORE, index);
			} else {
				main.visitVarInsn(ALOAD, index);
			}

			// We swap the values which is the value and the array
			// Then we get item 0 of the array
			// And store to it
			main.visitInsn(SWAP);
			main.visitIntInsn(ICONST_0, 0);
			main.visitInsn(SWAP);
			main.visitInsn(AASTORE);
		} else {
			main.visitVarInsn(ASTORE, index);
		}
	}

	public void createUpvalues(int pc, int firstSlot, int numSlots) {
		onStartOfLuaInstruction();

		for (int i = 0; i < numSlots; i++) {
			int slot = firstSlot + i;
			boolean isupcreate = pi.isUpvalueCreate(pc, slot);
			if (isupcreate) {
				int index = findSlotIndex(slot, true);
				methodCurrentNewUpvalueNil.inject(main);
				main.visitVarInsn(ASTORE, index);
			}
		}
	}

	public void convertToUpvalue(int pc, int slot) {
		onStartOfLuaInstruction();

		boolean isUpvalueAssing = pi.isUpvalueAssign(pc, slot);
		if (isUpvalueAssing) {
			int index = findSlotIndex(slot, false);

			// Load it from the slot, convert to an array and store it to the upvalue slot
			main.visitVarInsn(ALOAD, index);
			methodCurrentNewUpvalueValue.inject(main);
			int upvalueIndex = findSlotIndex(slot, true);
			main.visitVarInsn(ASTORE, upvalueIndex);
		}
	}

	private static String upvalueName(int upvalueIndex) {
		return PREFIX_UPVALUE + upvalueIndex;
	}

	public void loadUpvalue(int upvalueIndex) {
		onStartOfLuaInstruction();

		boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[upvalueIndex]);
		main.visitVarInsn(ALOAD, 0);

		if (isReadWrite) {
			// We get the first value of the array in <classname>.<upvalueName>
			main.visitFieldInsn(GETFIELD, className, upvalueName(upvalueIndex), TYPE_LOCALUPVALUE);
			main.visitInsn(ICONST_0);
			main.visitInsn(AALOAD);
		} else {
			// Not a 'proper' upvalue, so we just need to get the value itself
			main.visitFieldInsn(GETFIELD, className, upvalueName(upvalueIndex), TYPE_LUAVALUE);
		}
	}

	public void storeUpvalue(int pc, int upvalueIndex, int slot) {
		onStartOfLuaInstruction();

		boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[upvalueIndex]);
		main.visitVarInsn(ALOAD, 0);
		if (isReadWrite) {
			// We set the first value of the array in <classname>.<upvalueName>
			main.visitFieldInsn(GETFIELD, className, upvalueName(upvalueIndex), TYPE_LOCALUPVALUE);
			main.visitInsn(ICONST_0);
			loadLocal(pc, slot);
			main.visitInsn(AASTORE);
		} else {
			loadLocal(pc, slot);
			main.visitFieldInsn(PUTFIELD, className, upvalueName(upvalueIndex), TYPE_LUAVALUE);
		}
	}

	public void newTable(int b, int c) {
		onStartOfLuaInstruction();

		constantOpcode(main, b);
		constantOpcode(main, c);
		METHOD_TABLEOF_DIMS.inject(main);
	}

	public void loadEnv() {
		onStartOfLuaInstruction();

		main.visitVarInsn(ALOAD, 0);
		main.visitFieldInsn(GETFIELD, className, "env", TYPE_LUAVALUE);
	}

	public void loadVarargs() {
		onStartOfLuaInstruction();
		main.visitVarInsn(ALOAD, 1);
	}

	public void loadVarargs(int argindex) {
		loadVarargs();
		arg(argindex);
	}

	public void arg(int argindex) {
		onStartOfLuaInstruction();

		if (argindex == 1) {
			METHOD_VARARGS_ARG1.inject(main);
		} else {
			constantOpcode(main, argindex);
			METHOD_VARARGS_ARG.inject(main);
		}
	}

	private int getVarresultIndex() {
		if (varargsLocal < 0) varargsLocal = ++maxLocals;
		return varargsLocal;
	}

	public void loadVarresult() {
		onStartOfLuaInstruction();
		main.visitVarInsn(ALOAD, getVarresultIndex());
	}

	public void storeVarresult() {
		onStartOfLuaInstruction();
		main.visitVarInsn(ASTORE, getVarresultIndex());
	}

	public void subargs(int firstarg) {
		onStartOfLuaInstruction();
		constantOpcode(main, firstarg);
		METHOD_VARARGS_SUBARGS.inject(main);
	}

	public void getTable() {
		onStartOfLuaInstruction();
		METHOD_TABLE_GET.inject(main);
	}

	public void setTable() {
		onStartOfLuaInstruction();
		METHOD_TABLE_SET.inject(main);
	}

	public void unaryop(int o) {
		onStartOfLuaInstruction();

		String op;
		switch (o) {
			default:
			case Lua.OP_UNM:
				op = "min";
				break;
			case Lua.OP_NOT:
				op = "not";
				break;
			case Lua.OP_LEN:
				op = "len";
				break;
		}

		// TODO: More constants, less magic variables
		main.visitMethodInsn(INVOKEVIRTUAL, TYPE_LUAVALUE, op, "()" + TYPE_LUAVALUE, false);
	}

	public void binaryop(int o) {
		onStartOfLuaInstruction();

		String op;
		switch (o) {
			default:
			case Lua.OP_ADD:
				op = "add";
				break;
			case Lua.OP_SUB:
				op = "sub";
				break;
			case Lua.OP_MUL:
				op = "mul";
				break;
			case Lua.OP_DIV:
				op = "div";
				break;
			case Lua.OP_MOD:
				op = "mod";
				break;
			case Lua.OP_POW:
				op = "pow";
				break;
		}
		main.visitMethodInsn(INVOKEVIRTUAL, TYPE_LUAVALUE, op, "(" + TYPE_LUAVALUE + ")" + TYPE_LUAVALUE, false);
	}

	public void compareop(int o) {
		onStartOfLuaInstruction();

		String op;
		switch (o) {
			default:
			case Lua.OP_EQ:
				op = "eq_b";
				break;
			case Lua.OP_LT:
				op = "lt_b";
				break;
			case Lua.OP_LE:
				op = "lteq_b";
				break;
		}
		main.visitMethodInsn(INVOKEVIRTUAL, TYPE_LUAVALUE, op, "(" + TYPE_LUAVALUE + ")z" , false);
	}

	public void areturn() {
		onStartOfLuaInstruction();
		main.visitInsn(RETURN);
	}

	public void toBoolean() {
		onStartOfLuaInstruction();
		METHOD_VALUE_TO_BOOL.inject(main);
	}

	public void tostring() {
		onStartOfLuaInstruction();
		METHOD_BUFFER_TO_STR.inject(main);
	}

	public void isNil() {
		onStartOfLuaInstruction();
		METHOD_IS_NIL.inject(main);
	}

	public void testForLoop() {
		onStartOfLuaInstruction();
		METHOD_TESTFOR_B.inject(main);
	}

	public void loadArrayArgs(int pc, int firstslot, int nargs) {
		onStartOfLuaInstruction();

		constantOpcode(main, nargs);
		main.visitTypeInsn(ANEWARRAY, TYPE_LUAVALUE);
		for (int i = 0; i < nargs; i++) {
			main.visitInsn(DUP);
			constantOpcode(main, i);
			loadLocal(pc, firstslot++);
			main.visitInsn(AASTORE);
		}
	}

	public void newVarargs(int pc, int firstslot, int nargs) {
		onStartOfLuaInstruction();

		switch (nargs) {
			case 0:
				loadNone();
				break;
			case 1:
				loadLocal(pc, firstslot);
				break;
			case 2:
				loadLocal(pc, firstslot);
				loadLocal(pc, firstslot + 1);
				METHOD_VARARGS_ONE.inject(main);
				break;
			case 3:
				loadLocal(pc, firstslot);
				loadLocal(pc, firstslot + 1);
				loadLocal(pc, firstslot + 2);
				METHOD_VARARGS_TWO.inject(main);
				break;
			default:
				loadArrayArgs(pc, firstslot, nargs);
				METHOD_VARARGS_MANY.inject(main);
				break;
		}
	}

	public void newVarargsVarresult(int pc, int firstslot, int nslots) {
		onStartOfLuaInstruction();

		loadArrayArgs(pc, firstslot, nslots);
		loadVarresult();
		METHOD_VARARGS_MANY_VAR.inject(main);
	}

	public void call(int nargs) {
		onStartOfLuaInstruction();
		switch (nargs) {
			case 0:
				METHOD_CALL_NONE.inject(main);
				break;
			case 1:
				METHOD_CALL_ONE.inject(main);
				break;
			case 2:
				METHOD_CALL_TWO.inject(main);
				break;
			case 3:
				METHOD_CALL_THREE.inject(main);
				break;
			default:
				throw new IllegalArgumentException("can't call with " + nargs + " args");
		}
	}

	public void newTailcallVarargs() {
		onStartOfLuaInstruction();
		METHOD_TAILCALL.inject(main);
	}

	public void invoke(int nargs) {
		onStartOfLuaInstruction();

		switch (nargs) {
			case -1:
				METHOD_INVOKE_VAR.inject(main);
				break;
			case 0:
				METHOD_INVOKE_NONE.inject(main);
				break;
			case 1:
				METHOD_INVOKE_VAR.inject(main); // It is only one item so we can call it with a varargs
				break;
			case 2:
				METHOD_INVOKE_TWO.inject(main);
				break;
			case 3:
				METHOD_INVOKE_THREE.inject(main);
				break;
			default:
				throw new IllegalArgumentException("can't invoke with " + nargs + " args");
		}
	}


	// ------------------------ closures ------------------------

	public void closureCreate(String protoname) {
		onStartOfLuaInstruction();

		main.visitTypeInsn(NEW, protoname);
		main.visitInsn(DUP);
		main.visitMethodInsn(INVOKESPECIAL, "<init>", protoname, "()V", false);
		main.visitInsn(DUP);
		loadEnv();
		METHOD_SETENV.inject(main);
	}

	public void closureInitUpvalueFromUpvalue(String protoName, int newUpvalue, int upvalueIndex) {
		onStartOfLuaInstruction();

		boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[upvalueIndex]);

		String type = isReadWrite ? TYPE_LOCALUPVALUE : TYPE_LUAVALUE;
		String srcName = upvalueName(upvalueIndex);
		String destName = upvalueName(newUpvalue);

		main.visitVarInsn(ALOAD, 0);
		// Get from one field and set to the other
		main.visitFieldInsn(GETFIELD, className, srcName, type);
		main.visitFieldInsn(PUTFIELD, protoName, destName, type);
	}

	public void closureInitUpvalueFromLocal(String protoName, int newUpvalue, int pc, int srcSlot) {
		onStartOfLuaInstruction();

		boolean isReadWrite = pi.isReadWriteUpvalue(pi.vars[srcSlot][pc].upvalue);
		String type = isReadWrite ? TYPE_LOCALUPVALUE : TYPE_LUAVALUE;
		String destName = upvalueName(newUpvalue);
		int index = findSlotIndex(srcSlot, isReadWrite);

		main.visitVarInsn(ALOAD, index);
		main.visitFieldInsn(PUTFIELD, protoName, destName, type);
	}

	private Map<LuaValue, String> constants = new HashMap<>();

	public void loadConstant(LuaValue value) {
		onStartOfLuaInstruction();

		switch (value.type()) {
			case LuaValue.TNIL:
				loadNil();
				break;
			case LuaValue.TBOOLEAN:
				loadBoolean(value.toboolean());
				break;
			case LuaValue.TNUMBER:
			case LuaValue.TSTRING:
				String name = constants.get(value);
				if (name == null) {
					name = value.type() == LuaValue.TNUMBER ?
							value.isinttype() ?
									createLuaIntegerField(value.checkint()) :
									createLuaDoubleField(value.checkdouble()) :
							createLuaStringField(value.checkstring());
					constants.put(value, name);
				}
				main.visitFieldInsn(GETSTATIC, className, name, TYPE_LUAVALUE);
				break;
			default:
				throw new IllegalArgumentException("bad constant type: " + value.type());
		}
	}

	private String createLuaIntegerField(int value) {
		String name = PREFIX_CONSTANT + constants.size();
		writer.visitField(ACC_STATIC | ACC_FINAL, name, TYPE_LUAVALUE, null, null);

		constantOpcode(init, value);
		METHOD_VALUEOF_INT.inject(init);
		init.visitFieldInsn(PUTSTATIC, className, name, TYPE_LUAVALUE);
		return name;
	}

	private String createLuaDoubleField(double value) {
		String name = PREFIX_CONSTANT + constants.size();
		writer.visitField(ACC_STATIC | ACC_FINAL, name, TYPE_LUAVALUE, null, null);
		constantOpcode(init, value);
		METHOD_VALUEOF_DOUBLE.inject(init);
		init.visitFieldInsn(PUTSTATIC, className, name, TYPE_LUAVALUE);
		return name;
	}

	private String createLuaStringField(LuaString value) {
		String name = PREFIX_CONSTANT + constants.size();
		writer.visitField(ACC_STATIC | ACC_FINAL, name, TYPE_LUAVALUE, null, null);

		LuaString ls = value.checkstring();
		if (ls.isValidUtf8()) {
			init.visitLdcInsn(value.tojstring());
			METHOD_VALUEOF_STRING.inject(main);
		} else {
			char[] c = new char[ls.m_length];
			for (int j = 0; j < ls.m_length; j++) {
				c[j] = (char) (0xff & (int) (ls.m_bytes[ls.m_offset + j]));
			}
			init.visitLdcInsn(new String(c));
			METHOD_TO_CHARARRAY.inject(init);
			METHOD_VALUEOF_CHARARRAY.inject(init);
		}
		init.visitFieldInsn(PUTSTATIC, className, name, TYPE_LUAVALUE);
		return name;
	}

	// --------------------- branching support -------------------------
	public static final int BRANCH_GOTO = 1;
	public static final int BRANCH_IFNE = 2;
	public static final int BRANCH_IFEQ = 3;

	public void addBranch(int pc, int branchType, int targetPc) {
		onStartOfLuaInstruction();

		int type;
		switch (branchType) {
			default:
			case BRANCH_GOTO:
				type = GOTO;
				break;
			case BRANCH_IFNE:
				type = IFNE;
				break;
			case BRANCH_IFEQ:
				type = IFEQ;
				break;
		}

		// targets[pc] = targetPc;
		main.visitJumpInsn(type, branchDestinations[targetPc]);
	}

	/**
	 * This is a really ugly way of generating the branch instruction.
	 * Every Lua instruction is assigned one label, so jumping is possible.
	 * I want to maintain compatability with org.luaj.vm2.luajc.JavaGen so we need to keep it like this.
	 */
	private void onStartOfLuaInstruction() {
		if(currentLabel == null) {
			currentLabel = branchDestinations[pc];
			// TODO: Optimise this so we only visit needed labels
			main.visitLabel(currentLabel);
		}
	}

	public void onEndOfLuaInstruction(int pc) {
		this.pc = pc + 1;
		currentLabel = null;
	}

	private void resolveBranches() {
		/*
		int nc = p.code.length;
		for (int pc = 0; pc < nc; pc++) {
			if (branches[pc] != null) {
				int t = targets[pc];
				while (t < branchDestinations.length && branchDestinations[t] == null) {
					t++;
				}
				if (t >= branchDestinations.length) {
					throw new IllegalArgumentException("no target at or after " + targets[pc] + " op=" + Lua.GET_OPCODE(p.code[targets[pc]]));
				}
				branches[pc].setTarget(branchDestinations[t]);
			}
		}*/
	}

	public void setlistStack(int pc, int a0, int index0, int nvals) {
		onStartOfLuaInstruction();
		for (int i = 0; i < nvals; i++) {
			dup();
			constantOpcode(main, index0 + i);
			loadLocal(pc, a0 + i);
			METHOD_RAWSET.inject(main);
		}
	}

	public void setlistVarargs(int index, int resultbase) {
		onStartOfLuaInstruction();
		constantOpcode(main, index);
		loadVarresult();
		METHOD_RAWSET_LIST.inject(main);
	}

	public void concatvalue() {
		onStartOfLuaInstruction();
		METHOD_STRING_CONCAT.inject(main);
	}

	public void concatbuffer() {
		onStartOfLuaInstruction();
		METHOD_BUFFER_CONCAT.inject(main);
	}

	public void tobuffer() {
		onStartOfLuaInstruction();
		METHOD_VALUE_TO_BUFFER.inject(main);
	}

	public void tovalue() {
		onStartOfLuaInstruction();
		METHOD_BUFFER_TO_VALUE.inject(main);
	}
}
