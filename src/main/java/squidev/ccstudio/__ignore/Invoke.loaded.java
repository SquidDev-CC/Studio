mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Lorg/luaj/vm2/Varargs;)Lorg/luaj/vm2/Varargs;", null, null);
mv.visitCode();

// Load opcode
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "squidev/ccstudio/__ignore/Invoke", "opcode", "I");

Label helloBranch = new Label();
Label goodbyeBranch = new Label();
Label defaultBranch = new Label();
mv.visitLookupSwitchInsn(defaultBranch, new int[]{0, 1}, new Label[]{helloBranch, goodbyeBranch});

mv.visitLabel(helloBranch);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	// instance.sayHello();
	// return LuaValue.NONE;

	// Load instance
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, "squidev/ccstudio/__ignore/Invoke", "instance", "Ljava/lang/Object;");
	mv.visitTypeInsn(CHECKCAST, "squidev/ccstudio/__ignore/Invoke$SubThing");

	// Invoke sayHello
	mv.visitMethodInsn(INVOKEVIRTUAL, "squidev/ccstudio/__ignore/Invoke$SubThing", "sayHello", "()V", false);

	// Return LuaValue.None
	mv.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
	mv.visitInsn(ARETURN);


mv.visitLabel(goodbyeBranch);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	// if(args.narg() < 1) throw new LuaError("Expected double");

	// LuaValue val = args.arg(0);
	// if(!val.isnumber()) throw new LuaError("Expected double");
	// double var_0 = val.todouble();

	// double result = instance.sayGoodbye(var_0);
	// return LuaValue.valueOf(result);

	// if(args.narg() < 1)
	mv.visitVarInsn(ALOAD, 1);
	mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "narg", "()I", false);
	mv.visitInsn(ICONST_1);
	Label checkArgCount = new Label();
	mv.visitJumpInsn(IF_ICMPGE, checkArgCount);

	// throw new LuaError("Expected double");
	mv.visitTypeInsn(NEW, "org/luaj/vm2/LuaError");
	mv.visitInsn(DUP);
	mv.visitLdcInsn("Expected double");
	mv.visitMethodInsn(INVOKESPECIAL, "org/luaj/vm2/LuaError", "<init>", "(Ljava/lang/String;)V", false);
	mv.visitInsn(ATHROW);

	// LuaValue val = args.arg(0);
	mv.visitLabel(checkArgCount);
	mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	mv.visitVarInsn(ALOAD, 1);
	mv.visitInsn(ICONST_0);
	mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/Varargs", "arg", "(I)Lorg/luaj/vm2/LuaValue;", false);
	mv.visitVarInsn(ASTORE, 2);

	// if(!val.isnumber())
	mv.visitVarInsn(ALOAD, 2);
	mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "isnumber", "()Z", false);
	Label checkIsNumber = new Label();
	mv.visitJumpInsn(IFNE, checkIsNumber);

	// throw new LuaError("Expected double");
	mv.visitTypeInsn(NEW, "org/luaj/vm2/LuaError");
	mv.visitInsn(DUP);
	mv.visitLdcInsn("Expected double");
	mv.visitMethodInsn(INVOKESPECIAL, "org/luaj/vm2/LuaError", "<init>", "(Ljava/lang/String;)V", false);
	mv.visitInsn(ATHROW);

	// double var_0 = val.todouble();
	mv.visitLabel(checkIsNumber);
	mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"org/luaj/vm2/LuaValue"}, 0, null);
	mv.visitVarInsn(ALOAD, 2);
	mv.visitMethodInsn(INVOKEVIRTUAL, "org/luaj/vm2/LuaValue", "todouble", "()D", false);
	mv.visitVarInsn(DSTORE, 3);

	// double result = instance.sayGoodbye(var_0);
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, "squidev/ccstudio/__ignore/Invoke", "instance", "Ljava/lang/Object;");
	mv.visitTypeInsn(CHECKCAST, "squidev/ccstudio/__ignore/Invoke$SubThing");
	mv.visitVarInsn(DLOAD, 3);
	mv.visitMethodInsn(INVOKEVIRTUAL, "squidev/ccstudio/__ignore/Invoke$SubThing", "sayGoodbye", "(D)D", false);
	mv.visitVarInsn(DSTORE, 5);
	Label l9 = new Label();
	mv.visitLabel(l9);
	mv.visitLineNumber(31, l9);
	mv.visitVarInsn(DLOAD, 5);
	mv.visitMethodInsn(INVOKESTATIC, "org/luaj/vm2/LuaValue", "valueOf", "(D)Lorg/luaj/vm2/LuaNumber;", false);
	mv.visitInsn(ARETURN);
	mv.visitLabel(defaultBranch);
	mv.visitLineNumber(34, defaultBranch);
	mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
	mv.visitFieldInsn(GETSTATIC, "org/luaj/vm2/LuaValue", "NONE", "Lorg/luaj/vm2/LuaValue;");
	mv.visitInsn(ARETURN);

// Variables
// mv.visitLocalVariable("val", "Lorg/luaj/vm2/LuaValue;", null, l6, defaultBranch, 2);
// mv.visitLocalVariable("var_0", "D", null, l8, defaultBranch, 3);
// mv.visitLocalVariable("result", "D", null, l9, defaultBranch, 5);
// mv.visitLocalVariable("this", "Lsquidev/ccstudio/__ignore/Invoke;", null, l0, l10, 0);
// mv.visitLocalVariable("args", "Lorg/luaj/vm2/Varargs;", null, l0, l10, 1);
// mv.visitMaxs(3, 7);
mv.visitEnd();
