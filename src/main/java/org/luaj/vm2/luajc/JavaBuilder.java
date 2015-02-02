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

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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

	private static final String STR_VARARGS = Varargs.class.getName();
	private static final String STR_LUAVALUE = LuaValue.class.getName();
	private static final String STR_LUASTRING = LuaString.class.getName();
	private static final String STR_LUAINTEGER = LuaInteger.class.getName();
	private static final String STR_LUADOUBLE = LuaDouble.class.getName();
	private static final String STR_LUANUMBER = LuaNumber.class.getName();
	private static final String STR_LUABOOLEAN = LuaBoolean.class.getName();
	private static final String STR_LUATABLE = LuaTable.class.getName();
	private static final String STR_BUFFER = Buffer.class.getName();
	private static final String STR_STRING = String.class.getName();

	private static final ObjectType TYPE_VARARGS = new ObjectType(STR_VARARGS);
	private static final ObjectType TYPE_LUAVALUE = new ObjectType(STR_LUAVALUE);
	private static final ObjectType TYPE_LUASTRING = new ObjectType(STR_LUASTRING);
	private static final ObjectType TYPE_LUAINTEGER = new ObjectType(STR_LUAINTEGER);
	private static final ObjectType TYPE_LUADOUBLE = new ObjectType(STR_LUADOUBLE);
	private static final ObjectType TYPE_LUANUMBER = new ObjectType(STR_LUANUMBER);
	private static final ObjectType TYPE_LUABOOLEAN = new ObjectType(STR_LUABOOLEAN);
	private static final ObjectType TYPE_LUATABLE = new ObjectType(STR_LUATABLE);
	private static final ObjectType TYPE_BUFFER = new ObjectType(STR_BUFFER);

	private static final ArrayType TYPE_LOCALUPVALUE = new ArrayType(TYPE_LUAVALUE, 1);
	private static final ArrayType TYPE_CHARARRAY = new ArrayType(Type.CHAR, 1);

	private static final String TYPE_STR_LOCALUPVALUE = org.objectweb.asm.Type.getInternalName(LuaValue[].class);
	private static final String TYPE_STR_LUAVALUE = org.objectweb.asm.Type.getInternalName(LuaValue.class);


	private static final Class[] NO_INNER_CLASSES = {};

	/*private static final String STR_FUNCV = VarArgFunction.class.getName();
	private static final String STR_FUNC0 = ZeroArgFunction.class.getName();
	private static final String STR_FUNC1 = OneArgFunction.class.getName();
	private static final String STR_FUNC2 = TwoArgFunction.class.getName();
	private static final String STR_FUNC3 = ThreeArgFunction.class.getName();*/

	// argument list types
	private static final Type[] ARG_TYPES_NONE = {};
	private static final Type[] ARG_TYPES_INT = {Type.INT};
	private static final Type[] ARG_TYPES_DOUBLE = {Type.DOUBLE};
	private static final Type[] ARG_TYPES_STRING = {Type.STRING};
	private static final Type[] ARG_TYPES_CHARARRAY = {TYPE_CHARARRAY};
	private static final Type[] ARG_TYPES_VARARGS_INT = {TYPE_VARARGS, Type.INT};
	private static final Type[] ARG_TYPES_INT_LUAVALUE = {Type.INT, TYPE_LUAVALUE};
	private static final Type[] ARG_TYPES_INT_VARARGS = {Type.INT, TYPE_VARARGS};
	private static final Type[] ARG_TYPES_LUAVALUE_VARARGS = {TYPE_LUAVALUE, TYPE_VARARGS};
	private static final Type[] ARG_TYPES_LUAVALUE_LUAVALUE_VARARGS = {TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_VARARGS};
	private static final Type[] ARG_TYPES_LUAVALUEARRAY = {new ArrayType(TYPE_LUAVALUE, 1)};
	private static final Type[] ARG_TYPES_LUAVALUEARRAY_VARARGS = {new ArrayType(TYPE_LUAVALUE, 1), TYPE_VARARGS};
	private static final Type[] ARG_TYPES_LUAVALUE_LUAVALUE_LUAVALUE = {TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_LUAVALUE};
	private static final Type[] ARG_TYPES_VARARGS = {TYPE_VARARGS};
	private static final Type[] ARG_TYPES_LUAVALUE_LUAVALUE = {TYPE_LUAVALUE, TYPE_LUAVALUE};
	private static final Type[] ARG_TYPES_INT_INT = {Type.INT, Type.INT};
	private static final Type[] ARG_TYPES_LUAVALUE = {TYPE_LUAVALUE};
	private static final Type[] ARG_TYPES_BUFFER = {TYPE_BUFFER};

	// names, arg types for main prototype classes
/*	private static final String[] SUPER_NAME_N = {STR_FUNC0, STR_FUNC1, STR_FUNC2, STR_FUNC3, STR_FUNCV,};
	private static final ObjectType[] RETURN_TYPE_N = {TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_LUAVALUE, TYPE_VARARGS,};
	private static final Type[][] ARG_TYPES_N = {ARG_TYPES_NONE, ARG_TYPES_LUAVALUE, ARG_TYPES_LUAVALUE_LUAVALUE, ARG_TYPES_LUAVALUE_LUAVALUE_LUAVALUE, ARG_TYPES_VARARGS,};
	private static final String[][] ARG_NAMES_N = {{}, {"arg"}, {"arg1", "arg2"}, {"arg1", "arg2", "arg3"}, {"args"},};
	private static final String[] METH_NAME_N = {"call", "call", "call", "call", "onInvoke",};*/

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
	private static final TinyMethod METHOD_LUAVALUE_TABLEOF = TinyMethod.tryConstruct(LuaValue.class, "tableOf", Varargs.class, int.class);
	private static final TinyMethod METHOD_LUAVALUE_TABLEOF_DIMS = TinyMethod.tryConstruct(LuaValue.class, "tableOf", int.class, int.class);
	private static final TinyMethod METHOD_LUAVALUE_GET = TinyMethod.tryConstruct(LuaValue.class, "get", LuaValue.class);
	private static final TinyMethod METHOD_LUAVALUE_SET = TinyMethod.tryConstruct(LuaValue.class, "set", LuaValue.class);

	// Varargs
	private static final TinyMethod METHOD_VARARGS_ARG1 = TinyMethod.tryConstruct(Varargs.class, "arg1");
	private static final TinyMethod METHOD_VARARGS_ARG = TinyMethod.tryConstruct(Varargs.class, "arg", int.class);
	private static final TinyMethod METHOD_VARARGS_SUBARGS = TinyMethod.tryConstruct(Varargs.class, "subargs", int.class);

	// Type conversion
	private static final TinyMethod METHOD_LUAVALUE_TO_BOOL = TinyMethod.tryConstruct(LuaValue.class, "toboolean");
	private static final TinyMethod METHOD_BUFFER_TO_STR = TinyMethod.tryConstruct(Buffer.class, "tostring");

	// Booleans
	private static final TinyMethod METHOD_LUAVALUE_TESTFOR_B = TinyMethod.tryConstruct(LuaValue.class, "testfor_b", LuaValue.class, LuaValue.class);
	private static final TinyMethod METHOD_LUAVALUE_IS_NIL = TinyMethod.tryConstruct(LuaValue.class, "isnil");

	// TODO: Can this be LibFunction? Is there a performance change?
	private final TinyMethod methodCurrentNewUpvalueEmpty;
	private final TinyMethod methodCurrentNewUpvalueNil;
	private final TinyMethod methodCurrentNewUpvalueValue;

	// varable naming
	private static final String PREFIX_CONSTANT = "k";
	private static final String PREFIX_UPVALUE = "u";
	private static final String PREFIX_PLAIN_SLOT = "s";
	private static final String PREFIX_UPVALUE_SLOT = "a";
	private static final String NAME_VARRESULT = "v";

	// basic info
	private final ProtoInfo pi;
	private final Prototype p;
	private final String classname;

	// bcel variables
	private final ClassWriter writer;

	// main instruction list for the main function of this class
	private MethodVisitor init;
	private final MethodVisitor main;

	/**
	 * The program counter
	 */
	private int pc = 0;

	/**
	 * Max number of locals
	 */
	private int maxLocals = 0;

	/**
	 * The local index of the varargs result
	 */
	private int varargsLocal = -1;

	// the superclass arg count, 0-3 args, 4=varargs
	private int superclassType;
	private static int SUPERTYPE_VARARGS = 4;

	// storage for goto locations
	private final int[] targets;
	private final BranchInstruction[] branches;
	private final InstructionHandle[] branchDestHandles;
	private int beginningOfLuaInstruction;

	// hold vararg result
	private LocalVariableGen varresult = null;

	public JavaBuilder(ProtoInfo pi, String classname, String filename) {
		this.pi = pi;
		this.p = pi.prototype;
		this.classname = classname;

		// Create some more functions
		methodCurrentNewUpvalueEmpty = new TinyMethod(classname, "newupe", "()" + TYPE_STR_LOCALUPVALUE, true);
		methodCurrentNewUpvalueNil = new TinyMethod(classname, "newupn", "()" + TYPE_STR_LOCALUPVALUE, true);
		methodCurrentNewUpvalueValue = new TinyMethod(classname, "newupl", "(" + TYPE_STR_LUAVALUE + ")" + TYPE_STR_LOCALUPVALUE, true);

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
		writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

		// Check the name of the class. We have no interfaces and no generics
		writer.visit(V1_6, ACC_PUBLIC + ACC_SUPER, classname, null, superType.className, null);

		// Write the filename
		writer.visitSource(filename, null);

		// Create the fields
		for (int i = 0; i < p.nups; i++) {
			boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[i]);
			String uptype = isReadWrite ? TYPE_STR_LOCALUPVALUE : TYPE_STR_LUAVALUE;
			writer.visitField(0, upvalueName(i), uptype, null, null);
		}

		// Create the invoke method
		main = writer.visitMethod(ACC_PUBLIC + ACC_FINAL, superType.methodName, superType.signature, null, null);

		// Initialize the values in the slots
		initializeSlots();

		// initialize branching
		int nc = p.code.length;
		targets = new int[nc];
		branches = new BranchInstruction[nc];
		branchDestHandles = new InstructionHandle[nc];
	}

	public void initializeSlots() {
		int slot;
		createUpvalues(-1, 0, p.maxstacksize);
		MethodVisitor main = this.main;

		if (superclassType == SUPERTYPE_VARARGS) {
			for (slot = 0; slot < p.numparams; slot++) {
				if (pi.isInitialValueUsed(slot)) {
					main.visitVarInsn(ALOAD, 1);                    increment();
					constantOpcode(main, slot + 1);                 increment();
					METHOD_VARARGS_ARG.inject(main, INVOKEVIRTUAL); increment();
					storeLocal(-1, slot);
				}
			}
			boolean needsArg = ((p.is_vararg & Lua.VARARG_NEEDSARG) != 0);
			if (needsArg) {
				main.visitVarInsn(ALOAD, 1);                        increment();
				constantOpcode(main, p.numparams + 1);              increment();
				METHOD_LUAVALUE_TABLEOF.inject(main, INVOKESTATIC); increment();
				storeLocal(-1, slot++);
			} else if (p.numparams > 0) {
				main.visitVarInsn(ALOAD, 1);                        increment();
				constantOpcode(main, p.numparams + 1);              increment();
				METHOD_VARARGS_SUBARGS.inject(main, INVOKEVIRTUAL); increment();
				main.visitVarInsn(ASTORE, 1);
			}
		} else {
			// fixed arg function between 0 and 3 arguments
			for (slot = 0; slot < p.numparams; slot++) {
				this.plainSlotVars.put(slot, slot + 1);
				if (pi.isUpvalueCreate(-1, slot)) {
					main.visitVarInsn(ALOAD, 1);                    increment();
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

		// add class initializer
		if (!init.isEmpty()) {
			MethodGen mg = new MethodGen(Constants.ACC_STATIC, Type.VOID,
					ARG_TYPES_NONE, new String[]{}, "<clinit>",
					cg.getClassName(), init, cg.getConstantPool());
			init.append(InstructionConstants.RETURN);
			mg.setMaxStack();
			cg.addMethod(mg.getMethod());
			init.dispose();
		}

		// add default constructor
		cg.addEmptyConstructor(Constants.ACC_PUBLIC);

		// gen method
		resolveBranches();
		main.visitMaxs(0, 0);
		main.visitEnd();

		// convert to class bytes
		byte[] bytes = writer.toByteArray();
		if (Config.verifySources) {
			CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.out));
		}
		return bytes;
	}

	public void dup() {
		main.visitInsn(DUP);
		increment();
	}

	public void pop() {
		main.visitInsn(POP);
		increment();
	}

	public void loadNil() {
		main.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NIL", "Lorg/luaj/vm2/LuaValue;");
		increment();
	}

	public void loadNone() {
		main.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
		increment();
	}

	public void loadBoolean(boolean b) {
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
		boolean isUpvalue = pi.isUpvalueRefer(pc, slot);
		int index = findSlotIndex(slot, isUpvalue);

		main.visitVarInsn(ALOAD, index); increment();
		if (isUpvalue) {
			main.visitInsn(ICONST_0);    increment();
			main.visitInsn(AALOAD);      increment();
		}
	}

	public void storeLocal(int pc, int slot) {
		boolean isUpvalue = pi.isUpvalueAssign(pc, slot);
		int index = findSlotIndex(slot, isUpvalue);
		if (isUpvalue) {
			boolean isUpCreate = pi.isUpvalueCreate(pc, slot);
			if (isUpCreate) {
				// If we are creating the upvalue for the first time then we call LibFunction.newupe (but actually call
				// <className>.newupe but I need to check that). The we duplicate the object, so it remains on the stack
				// and store it
				methodCurrentNewUpvalueEmpty.inject(main); increment();
				main.visitInsn(DUP);                  increment();
				main.visitVarInsn(ASTORE, index);     increment();
			} else {
				main.visitVarInsn(ALOAD, index);      increment();
			}

			// We swap the values which is the value and the array
			// Then we get item 0 of the array
			// And store to it
			main.visitInsn(SWAP);             increment();
			main.visitIntInsn(ICONST_0, 0);   increment();
			main.visitInsn(SWAP);             increment();
			main.visitInsn(AASTORE);          increment();
		} else {
			main.visitVarInsn(ASTORE, index); increment();
		}
	}

	public void createUpvalues(int pc, int firstSlot, int numSlots) {
		for (int i = 0; i < numSlots; i++) {
			int slot = firstSlot + i;
			boolean isupcreate = pi.isUpvalueCreate(pc, slot);
			if (isupcreate) {
				int index = findSlotIndex(slot, true);
				methodCurrentNewUpvalueNil.inject(main); increment();
				main.visitVarInsn(ASTORE, index);     increment();
			}
		}
	}

	public void convertToUpvalue(int pc, int slot) {
		boolean isUpvalueAssing = pi.isUpvalueAssign(pc, slot);
		if (isUpvalueAssing) {
			int index = findSlotIndex(slot, false);

			// Load it from the slot, convert to an array and store it to the upvalue slot
			main.visitVarInsn(ALOAD, index);              increment();
			methodCurrentNewUpvalueValue.inject(main);    increment();
			int upvalueIndex = findSlotIndex(slot, true);
			main.visitVarInsn(ASTORE, upvalueIndex);      increment();
		}
	}

	private static String upvalueName(int upvalueIndex) {
		return PREFIX_UPVALUE + upvalueIndex;
	}

	public void loadUpvalue(int upvalueIndex) {
		boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[upvalueIndex]);
		main.visitVarInsn(ALOAD, 0); increment();

		if (isReadWrite) {
			// We get the first value of the array in <classname>.<upvalueName>
			main.visitFieldInsn(GETFIELD, classname, upvalueName(upvalueIndex), TYPE_STR_LOCALUPVALUE); increment();
			main.visitInsn(ICONST_0); increment();
			main.visitInsn(AALOAD);   increment();
		} else {
			// Not a 'proper' upvalue, so we just need to get the value itself
			main.visitFieldInsn(GETFIELD, classname, upvalueName(upvalueIndex), TYPE_STR_LUAVALUE);     increment();
		}
	}

	public void storeUpvalue(int pc, int upvalueIndex, int slot) {
		boolean isReadWrite = pi.isReadWriteUpvalue(pi.upvals[upvalueIndex]);
		main.visitVarInsn(ALOAD, 0); increment();
		if (isReadWrite) {
			// We set the first value of the array in <classname>.<upvalueName>
			main.visitFieldInsn(GETFIELD, classname, upvalueName(upvalueIndex), TYPE_STR_LOCALUPVALUE); increment();
			main.visitInsn(ICONST_0); increment();
			loadLocal(pc, slot);
			main.visitInsn(AASTORE);  increment();
		} else {
			loadLocal(pc, slot);
			main.visitFieldInsn(PUTFIELD, classname, upvalueName(upvalueIndex), TYPE_STR_LUAVALUE);     increment();
		}
	}

	public void newTable(int b, int c) {
		constantOpcode(main, b); increment();
		constantOpcode(main, c); increment();
		METHOD_LUAVALUE_TABLEOF_DIMS.inject(main); increment();
	}

	public void loadEnv() {
		main.visitVarInsn(ALOAD, 0); increment();
		main.visitFieldInsn(GETFIELD, classname, "env", TYPE_STR_LUAVALUE); increment();
	}

	public void loadVarargs() {
		main.visitVarInsn(ALOAD, 1); increment();
	}

	public void loadVarargs(int argindex) {
		loadVarargs();
		arg(argindex);
	}

	public void arg(int argindex) {
		if (argindex == 1) {
			METHOD_VARARGS_ARG1.inject(main); increment();
		} else {
			constantOpcode(main, argindex);   increment();
			METHOD_VARARGS_ARG.inject(main);  increment();
		}
	}

	private int getVarresultIndex() {
		if (varargsLocal < 0) varargsLocal = ++maxLocals;
		return varargsLocal;
	}

	public void loadVarresult() {
		main.visitVarInsn(ALOAD, getVarresultIndex()); increment();
	}

	public void storeVarresult() {
		main.visitVarInsn(ASTORE, getVarresultIndex()); increment();
	}

	public void subargs(int firstarg) {
		constantOpcode(main, firstarg);      increment();
		METHOD_VARARGS_SUBARGS.inject(main); increment();
	}

	public void getTable() {
		METHOD_LUAVALUE_GET.inject(main); increment();
	}

	public void setTable() {
		METHOD_LUAVALUE_SET.inject(main); increment();
	}

	public void unaryop(int o) {
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
		main.visitMethodInsn(INVOKEVIRTUAL, TYPE_STR_LUAVALUE, op, "()" + TYPE_STR_LUAVALUE, false);
		increment();
	}

	public void binaryop(int o) {
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
		main.visitMethodInsn(INVOKEVIRTUAL, TYPE_STR_LUAVALUE, op, "(" + TYPE_STR_LUAVALUE + ")" + TYPE_STR_LUAVALUE, false);
		increment();
	}

	public void compareop(int o) {
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
		main.visitMethodInsn(INVOKEVIRTUAL, TYPE_STR_LUAVALUE, op, "(" + TYPE_STR_LUAVALUE + ")z" , false);
		increment();
	}

	public void areturn() {
		main.visitInsn(RETURN);
		increment();
	}

	public void toBoolean() {
		METHOD_LUAVALUE_TO_BOOL.inject(main);
		increment();
	}

	public void tostring() {
		METHOD_BUFFER_TO_STR.inject(main);
		increment();
	}

	public void isNil() {
		METHOD_LUAVALUE_IS_NIL.inject(main);
		increment();
	}

	public void testForLoop() {
		append(factory.createInvoke(STR_LUAVALUE, "testfor_b", Type.BOOLEAN, ARG_TYPES_LUAVALUE_LUAVALUE, Constants.INVOKEVIRTUAL));
	}

	public void loadArrayArgs(int pc, int firstslot, int nargs) {
		append(new PUSH(cp, nargs));
		append(new ANEWARRAY(cp.addClass(STR_LUAVALUE)));
		for (int i = 0; i < nargs; i++) {
			append(InstructionConstants.DUP);
			append(new PUSH(cp, i));
			loadLocal(pc, firstslot++);
			append(new AASTORE());
		}
	}

	public void newVarargs(int pc, int firstslot, int nargs) {
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
				append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, ARG_TYPES_LUAVALUE_VARARGS, Constants.INVOKESTATIC));
				break;
			case 3:
				loadLocal(pc, firstslot);
				loadLocal(pc, firstslot + 1);
				loadLocal(pc, firstslot + 2);
				append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, ARG_TYPES_LUAVALUE_LUAVALUE_VARARGS, Constants.INVOKESTATIC));
				break;
			default:
				loadArrayArgs(pc, firstslot, nargs);
				append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, ARG_TYPES_LUAVALUEARRAY, Constants.INVOKESTATIC));
				break;
		}
	}

	public void newVarargsVarresult(int pc, int firstslot, int nslots) {
		loadArrayArgs(pc, firstslot, nslots);
		loadVarresult();
		append(factory.createInvoke(STR_LUAVALUE, "varargsOf", TYPE_VARARGS, ARG_TYPES_LUAVALUEARRAY_VARARGS, Constants.INVOKESTATIC));
	}

	public void call(int nargs) {
		switch (nargs) {
			case 0:
				append(factory.createInvoke(STR_LUAVALUE, "call", TYPE_LUAVALUE, ARG_TYPES_NONE, Constants.INVOKEVIRTUAL));
				break;
			case 1:
				append(factory.createInvoke(STR_LUAVALUE, "call", TYPE_LUAVALUE, ARG_TYPES_LUAVALUE, Constants.INVOKEVIRTUAL));
				break;
			case 2:
				append(factory.createInvoke(STR_LUAVALUE, "call", TYPE_LUAVALUE, ARG_TYPES_LUAVALUE_LUAVALUE, Constants.INVOKEVIRTUAL));
				break;
			case 3:
				append(factory.createInvoke(STR_LUAVALUE, "call", TYPE_LUAVALUE, ARG_TYPES_LUAVALUE_LUAVALUE_LUAVALUE, Constants.INVOKEVIRTUAL));
				break;
			default:
				throw new IllegalArgumentException("can't call with " + nargs + " args");
		}
	}

	public void newTailcallVarargs() {
		append(factory.createInvoke(STR_LUAVALUE, "tailcallOf", TYPE_VARARGS, ARG_TYPES_LUAVALUE_VARARGS, Constants.INVOKESTATIC));
	}

	public void invoke(int nargs) {
		switch (nargs) {
			case -1:
				append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, ARG_TYPES_VARARGS, Constants.INVOKEVIRTUAL));
				break;
			case 0:
				append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, ARG_TYPES_NONE, Constants.INVOKEVIRTUAL));
				break;
			case 1:
				append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, ARG_TYPES_VARARGS, Constants.INVOKEVIRTUAL));
				break;
			case 2:
				append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, ARG_TYPES_LUAVALUE_VARARGS, Constants.INVOKEVIRTUAL));
				break;
			case 3:
				append(factory.createInvoke(STR_LUAVALUE, "invoke", TYPE_VARARGS, ARG_TYPES_LUAVALUE_LUAVALUE_VARARGS, Constants.INVOKEVIRTUAL));
				break;
			default:
				throw new IllegalArgumentException("can't invoke with " + nargs + " args");
		}
	}


	// ------------------------ closures ------------------------

	public void closureCreate(String protoname) {
		append(factory.createNew(new ObjectType(protoname)));
		append(InstructionConstants.DUP);
		append(factory.createInvoke(protoname, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
		append(InstructionConstants.DUP);
		loadEnv();
		append(factory.createInvoke(STR_LUAVALUE, "setfenv", Type.VOID, ARG_TYPES_LUAVALUE, Constants.INVOKEVIRTUAL));
	}

	public void closureInitUpvalueFromUpvalue(String protoname, int newup, int upindex) {
		boolean isrw = pi.isReadWriteUpvalue(pi.upvals[upindex]);
		Type uptype = isrw ? TYPE_LOCALUPVALUE : TYPE_LUAVALUE;
		String srcname = upvalueName(upindex);
		String destname = upvalueName(newup);
		append(InstructionConstants.THIS);
		append(factory.createFieldAccess(classname, srcname, uptype, Constants.GETFIELD));
		append(factory.createFieldAccess(protoname, destname, uptype, Constants.PUTFIELD));
	}

	public void closureInitUpvalueFromLocal(String protoname, int newup, int pc, int srcslot) {
		boolean isrw = pi.isReadWriteUpvalue(pi.vars[srcslot][pc].upvalue);
		Type uptype = isrw ? TYPE_LOCALUPVALUE : TYPE_LUAVALUE;
		String destname = upvalueName(newup);
		int index = findSlotIndex(srcslot, isrw);
		append(new ALOAD(index));
		append(factory.createFieldAccess(protoname, destname, uptype, Constants.PUTFIELD));
	}

	private Map<LuaValue, String> constants = new HashMap<LuaValue, String>();

	public void loadConstant(LuaValue value) {
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
				append(factory.createGetStatic(classname, name, TYPE_LUAVALUE));
				break;
			default:
				throw new IllegalArgumentException("bad constant type: " + value.type());
		}
	}

	private String createLuaIntegerField(int value) {
		String name = PREFIX_CONSTANT + constants.size();
		FieldGen fg = new FieldGen(Constants.ACC_STATIC | Constants.ACC_FINAL,
				TYPE_LUAVALUE, name, cp);
		cg.addField(fg.getField());
		init.append(new PUSH(cp, value));
		init.append(factory.createInvoke(STR_LUAVALUE, "valueOf",
				TYPE_LUAINTEGER, ARG_TYPES_INT, Constants.INVOKESTATIC));
		init.append(factory.createPutStatic(classname, name, TYPE_LUAVALUE));
		return name;
	}

	private String createLuaDoubleField(double value) {
		String name = PREFIX_CONSTANT + constants.size();
		FieldGen fg = new FieldGen(Constants.ACC_STATIC | Constants.ACC_FINAL,
				TYPE_LUAVALUE, name, cp);
		cg.addField(fg.getField());
		init.append(new PUSH(cp, value));
		init.append(factory.createInvoke(STR_LUAVALUE, "valueOf",
				TYPE_LUANUMBER, ARG_TYPES_DOUBLE, Constants.INVOKESTATIC));
		init.append(factory.createPutStatic(classname, name, TYPE_LUAVALUE));
		return name;
	}

	private String createLuaStringField(LuaString value) {
		String name = PREFIX_CONSTANT + constants.size();
		FieldGen fg = new FieldGen(Constants.ACC_STATIC | Constants.ACC_FINAL,
				TYPE_LUAVALUE, name, cp);
		cg.addField(fg.getField());
		LuaString ls = value.checkstring();
		if (ls.isValidUtf8()) {
			init.append(new PUSH(cp, value.tojstring()));
			init.append(factory.createInvoke(STR_LUASTRING, "valueOf",
					TYPE_LUASTRING, ARG_TYPES_STRING, Constants.INVOKESTATIC));
		} else {
			char[] c = new char[ls.m_length];
			for (int j = 0; j < ls.m_length; j++)
				c[j] = (char) (0xff & (int) (ls.m_bytes[ls.m_offset + j]));
			init.append(new PUSH(cp, new String(c)));
			init.append(factory.createInvoke(STR_STRING, "toCharArray",
					TYPE_CHARARRAY, Type.NO_ARGS,
					Constants.INVOKEVIRTUAL));
			init.append(factory.createInvoke(STR_LUASTRING, "valueOf",
					TYPE_LUASTRING, ARG_TYPES_CHARARRAY,
					Constants.INVOKESTATIC));
		}
		init.append(factory.createPutStatic(classname, name, TYPE_LUAVALUE));
		return name;
	}

	// --------------------- branching support -------------------------
	public static final int BRANCH_GOTO = 1;
	public static final int BRANCH_IFNE = 2;
	public static final int BRANCH_IFEQ = 3;

	public void addBranch(int pc, int branchType, int targetpc) {
		switch (branchType) {
			default:
			case BRANCH_GOTO:
				branches[pc] = new GOTO(null);
				break;
			case BRANCH_IFNE:
				branches[pc] = new IFNE(null);
				break;
			case BRANCH_IFEQ:
				branches[pc] = new IFEQ(null);
				break;
		}
		targets[pc] = targetpc;
		append(branches[pc]);
	}


	private void increment() {
		++pc;
		if(beginningOfLuaInstruction < 0) {
			beginningOfLuaInstruction = pc;
		}
	}

	public void onEndOfLuaInstruction(int pc) {
		branchDestHandles[pc] = beginningOfLuaInstruction;
		beginningOfLuaInstruction = -1;
	}

	private void resolveBranches() {
		int nc = p.code.length;
		for (int pc = 0; pc < nc; pc++) {
			if (branches[pc] != null) {
				int t = targets[pc];
				while (t < branchDestHandles.length && branchDestHandles[t] == null)
					t++;
				if (t >= branchDestHandles.length)
					throw new IllegalArgumentException("no target at or after " + targets[pc] + " op=" + Lua.GET_OPCODE(p.code[targets[pc]]));
				branches[pc].setTarget(branchDestHandles[t]);
			}
		}
	}

	public void setlistStack(int pc, int a0, int index0, int nvals) {
		for (int i = 0; i < nvals; i++) {
			dup();
			append(new PUSH(cp, index0 + i));
			loadLocal(pc, a0 + i);
			append(factory.createInvoke(STR_LUAVALUE, "rawset", Type.VOID, ARG_TYPES_INT_LUAVALUE, Constants.INVOKEVIRTUAL));
		}
	}

	public void setlistVarargs(int index0, int vresultbase) {
		append(new PUSH(cp, index0));
		loadVarresult();
		append(factory.createInvoke(STR_LUAVALUE, "rawsetlist", Type.VOID, ARG_TYPES_INT_VARARGS, Constants.INVOKEVIRTUAL));
	}

	public void concatvalue() {
		append(factory.createInvoke(STR_LUAVALUE, "concat", TYPE_LUAVALUE, ARG_TYPES_LUAVALUE, Constants.INVOKEVIRTUAL));
	}

	public void concatbuffer() {
		append(factory.createInvoke(STR_LUAVALUE, "concat", TYPE_BUFFER, ARG_TYPES_BUFFER, Constants.INVOKEVIRTUAL));
	}

	public void tobuffer() {
		append(factory.createInvoke(STR_LUAVALUE, "buffer", TYPE_BUFFER, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	}

	public void tovalue() {
		append(factory.createInvoke(STR_BUFFER, "value", TYPE_LUAVALUE, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	}
}