import ast.*;

import java.util.List;

public class CgenEmitVisitor extends CgenVisitor<String, String> {

    /* Emit code for expressions */
    CgenEnv env;

    //  target: if there is any choice, put the result here, but
    //  there are no guarantees.
    //  Use forceDest instead if you really care.
    //  Return value: the name of the register holding the result.
    //  Possibly the same as target.

    // Default implementation
    @Override
    public String visit(AssignNode node, String _unused) {
        Cgen.VarInfo lhs = env.vars.lookup(node.getName());
        String rhs_value = node.getExpr().accept(this, CgenConstants.ACC);
        lhs.emitUpdate(rhs_value);
        return CgenConstants.ACC;
    }

    //// Dynamic dispatch:
    //    1. The arguments are evaluated and pushed on the stack.
    //    2. The dispatch expression is evaluated, and the result put in $a0.
    //    3. The dipatch expression is tested for void.
    //    4.     If void, computation is aborted.
    //    5. The dispatch table of the dispatch value is loaded.
    //    6. The dispatch table is indexed with the method offset.
    //    7. Jump to the method.
    //// Static dispatch has the same steps as normal dispatch, except
    //// the dispatch table is taken from the user-specified class.

    // Default implementation
    @Override
    public String visit(DispatchNode node, String _unused) {
        Symbol classname = node.getExpr().getType();
        if (classname == TreeConstants.SELF_TYPE)
            classname = env.getClassname();
        CgenNode c = Cgen.classTable.get(classname);
        Cgen.MethodInfo minfo = c.env.methods.lookup(node.getName());
        for (ExpressionNode e : node.getActuals()) {
            e.accept(this, CgenConstants.ACC);
            Cgen.emitter.emitPush(CgenConstants.ACC);
        }
        forceDest(node.getExpr(), CgenConstants.ACC);
        if (Flags.cgen_debug)
            System.err.println("    Dispatch to " + node.getName());
        int lab = CgenEnv.getFreshLabel();
        Cgen.emitter.emitBne(CgenConstants.ACC, CgenConstants.ZERO, lab); // test for void
        Cgen.emitter.emitLoadString(CgenConstants.ACC, env.getFilename());
        Cgen.emitter.emitLoadImm(CgenConstants.T1, node.getLineNumber());
        Cgen.emitter.emitDispatchAbort();
        Cgen.emitter.emitLabelDef(lab);
        Cgen.emitter.emitLoad(CgenConstants.T1, CgenConstants.DISPTABLE_OFFSET, CgenConstants.ACC);
        Cgen.emitter.emitLoad(CgenConstants.T1, minfo.getOffset(), CgenConstants.T1);
        Cgen.emitter.emitJalr(CgenConstants.T1);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(StaticDispatchNode node, String _unused) {
        /* TODO */
        // use the declared type T instead of the actual expr type
        Symbol classname = node.getType_name();
        if (classname == TreeConstants.SELF_TYPE)
            classname = env.getClassname();
        CgenNode c = Cgen.classTable.get(classname);
        Cgen.MethodInfo minfo = c.env.methods.lookup(node.getName());
        //load expression then push onto stack
        for (ExpressionNode e : node.getActuals()) {
            e.accept(this, CgenConstants.ACC);
            Cgen.emitter.emitPush(CgenConstants.ACC);
        }

        forceDest(node.getExpr(), CgenConstants.ACC);

        int lab = CgenEnv.getFreshLabel();
        Cgen.emitter.emitBne(CgenConstants.ACC, CgenConstants.ZERO, lab); // test for void
        Cgen.emitter.emitLoadString(CgenConstants.ACC, env.getFilename());
        Cgen.emitter.emitLoadImm(CgenConstants.T1, node.getLineNumber());
        Cgen.emitter.emitDispatchAbort();
        Cgen.emitter.emitLabelDef(lab);
        //Cgen.emitter.emitDebugPrint("test3");
        //	la	$t1 Base_dispTab
        Cgen.emitter.emitLoadAddress(CgenConstants.T1, node.getType_name() + CgenConstants.DISPTAB_SUFFIX);
        //Cgen.emitter.emitDebugPrint("test4");
        Cgen.emitter.emitLoad(CgenConstants.T1, minfo.getOffset(), CgenConstants.T1);
        Cgen.emitter.emitJalr(CgenConstants.T1);
        return CgenConstants.ACC;
    }

    // The cases are tested in the order
    // of most specific to least specific.  Since tags are assigned
    // in depth-first order with the root being assigned 0, tests for higher-numbered
    // classes should be emitted before lower-numbered classes.

    // Default implementation
    @Override
    public String visit(CaseNode node, String _unused) {
        int out_label = CgenEnv.getFreshLabel();

        node.getExpr().accept(this, CgenConstants.ACC);
        int lab = CgenEnv.getFreshLabel();
        Cgen.emitter.emitBne(CgenConstants.ACC, CgenConstants.ZERO, lab); // test for void
        Cgen.emitter.emitLoadString(CgenConstants.ACC, env.getFilename());
        Cgen.emitter.emitLoadImm(CgenConstants.T1, node.getLineNumber());
        Cgen.emitter.emitCaseAbort2();
        Cgen.emitter.emitLabelDef(lab);
        Cgen.emitter.emitLoad(CgenConstants.T2, CgenConstants.TAG_OFFSET, CgenConstants.ACC); // fetch the class tag

        for (int class_num = CgenEnv.getLastTag() - 1; class_num >= 0; class_num--)
            for (BranchNode b : node.getCases()) {
                int tag = Cgen.classTable.get(b.getType_decl()).env.getClassTag();
                if (class_num == tag) {
                    if (Flags.cgen_debug)
                        System.err.println("    Coding case " + b.getType_decl());
                    // result is in ACC
                    // r_newvar is the value that we did the case on.  It will be bound to the new var.
                    String r_newvar = CgenConstants.ACC;

                    lab = CgenEnv.getFreshLabel();
                    CgenEnv downcast = Cgen.classTable.get(b.getType_decl()).env;
                    int class_tag = downcast.getClassTag();
                    int last_tag = downcast.getMaxChildTag();

                    Cgen.emitter.emitBlti(CgenConstants.T2, class_tag, lab);
                    Cgen.emitter.emitBgti(CgenConstants.T2, last_tag, lab);
                    env.addLocal(b.getName());
                    env.vars.lookup(b.getName()).emitUpdate(r_newvar);
                    forceDest(b.getExpr(), CgenConstants.ACC);
                    env.removeLocal();
                    Cgen.emitter.emitBranch(out_label);
                    Cgen.emitter.emitLabelDef(lab);
                }
            }
        Cgen.emitter.emitCaseAbort();
        Cgen.emitter.emitLabelDef(out_label);
        return CgenConstants.ACC;
    }

    // Default implementation
    @Override
    public String visit(LetNode node, String _unused) {
        // r_newvar is the register to which we think the new variable will be
        //  assigned.
        // r_newvar is null if register allocation is disabled or no regs availible.
        //r_init is the register that holds the result of the init expr.  We'd like
        //  r_init to be the same as r_newvar.
        String r_newvar = CgenConstants.getRegister(env.getNextTempOffset());

        String r_init = r_newvar;
        if (r_init == null) {
            r_init = CgenConstants.ACC;
        }

        if (node.getInit() instanceof NoExpressionNode) {
            // if variable is not initialised, use default initialisation
            // see manual page 25
            if (TreeConstants.Int == node.getType_decl()) {
                Cgen.emitter.emitPartialLoadAddress(r_init);
                Cgen.emitter.codeRefInt(StringTable.inttable.get("0"));
                Cgen.emitter.emitNewline();
            } else if (TreeConstants.Str == node.getType_decl()) {
                Cgen.emitter.emitPartialLoadAddress(r_init);
                Cgen.emitter.codeRefString(StringTable.stringtable.get(""));
                Cgen.emitter.emitNewline();
            } else if (TreeConstants.Bool == node.getType_decl()) {
                Cgen.emitter.emitPartialLoadAddress(r_init);
                Cgen.emitter.codeRef(false);
                Cgen.emitter.emitNewline();
            } else {
                r_init = CgenConstants.ZERO;
            }
        } else {
            node.getInit().accept(this, r_init);
            r_init = CgenConstants.ACC;
        }

        //Register r_init now holds the location of the value to which newvar should
        //be initialized.  Hopefully, r_init and newvar are one and the same, in
        //which case the code_update is a nop.
        env.addLocal(node.getIdentifier());
        Cgen.VarInfo newvar = env.vars.lookup(node.getIdentifier());
        newvar.emitUpdate(r_init);

        //test that r_newvar really contains the register to which newvar
        //was assigned.
        assert (CgenConstants.regEq(newvar.getRegister(), r_newvar));

        node.getBody().accept(this, CgenConstants.ACC);
        env.removeLocal();
        return CgenConstants.ACC;
    }

    @Override
    public String visit(NewNode node, String _unused) {
        /* TODO */
        Symbol name = node.getType_name();
        if (name == TreeConstants.SELF_TYPE) {
            name = env.getClassname();
            //Cgen.emitter.emitComment("self type");
            // la	$t1 class_objTab
            Cgen.emitter.emitLoadAddress(CgenConstants.T1, CgenConstants.CLASSOBJTAB);
            // lw	$t2 0($s0)
            Cgen.emitter.emitLoad(CgenConstants.T2, 0, CgenConstants.SELF);
            // sll	$t2 $t2 3
            Cgen.emitter.emitSll(CgenConstants.T2, CgenConstants.T2, 3);
            // addu	$t1 $t1 $t2
            Cgen.emitter.emitAddu(CgenConstants.T1, CgenConstants.T1, CgenConstants.T2);
            // move	$s1 $t1
            Cgen.emitter.emitMove(CgenConstants.regNames[0], CgenConstants.T1);
            // lw	$a0 0($t1)
            Cgen.emitter.emitLoad(CgenConstants.ACC, 0, CgenConstants.T1);
            // jal	Object.copy
            Cgen.emitter.emitJal(CgenConstants.OBJECT_COPY);
            // lw	$t1 4($s1)
            Cgen.emitter.emitLoad(CgenConstants.T1, 1, CgenConstants.regNames[0]);
            // jalr		$t1
            Cgen.emitter.emitJalr(CgenConstants.T1);
        }
        else{
            // la	$a0 IO_protObj
            Cgen.emitter.emitLoadAddress(CgenConstants.ACC, name + CgenConstants.PROTOBJ_SUFFIX);

            // Object.copy copies the object currently at $a0
            // the new object will be replaced at $a0
            // see T10-runtime-v0.pdf

            // jal	Object.copy
            Cgen.emitter.emitJal(CgenConstants.OBJECT_COPY);
            // jal	IO_init
            Cgen.emitter.emitJal(name + CgenConstants.CLASSINIT_SUFFIX);
        }
        // the new object is now at $a0, no further work is needed
        return CgenConstants.ACC;
    }

    @Override
    public String visit(CondNode node, String _unused) {
        int labelElse = CgenEnv.getFreshLabel();
        int labelEndIf = CgenEnv.getFreshLabel();

        // evaluate the condition
        node.getCond().accept(this, CgenConstants.ACC);

        // load the value (offset 12) of the boolean into t1
        Cgen.emitter.emitLoadVal(CgenConstants.T1, CgenConstants.ACC);
        // jump to ELSE branch if false
        Cgen.emitter.emitBeqz(CgenConstants.T1, labelElse);

        // we didn't jump to the ELSE branch
        // evaluate the THEN branch
        node.getThenExpr().accept(this, CgenConstants.ACC);
        // jump to ENDIF
        Cgen.emitter.emitBranch(labelEndIf);

        // we jumped here from Beqz
        // do the ELSE branch
        Cgen.emitter.emitLabelDef(labelElse);
        node.getElseExpr().accept(this, CgenConstants.ACC);

        // ENDIF label here
        Cgen.emitter.emitLabelDef(labelEndIf);

        return CgenConstants.ACC;
    }

    @Override
    public String visit(LoopNode node, String _unused) {

        int loopStartLabel = CgenEnv.getFreshLabel();
        int loopEndLabel = CgenEnv.getFreshLabel();

        // start of loop
        Cgen.emitter.emitLabelDef(loopStartLabel);

        // evaluate the condition
        node.getCond().accept(this, CgenConstants.ACC);
        // load the value (offset 12) of the boolean into a0
        Cgen.emitter.emitLoadVal(CgenConstants.T1, CgenConstants.ACC);

        // jump to loop end if false
        Cgen.emitter.emitBeqz(CgenConstants.T1, loopEndLabel);

        // evaluate the body
        node.getBody().accept(this, CgenConstants.ACC);
        // jump back to loop start
        Cgen.emitter.emitBranch(loopStartLabel);

        // label loop end
        Cgen.emitter.emitLabelDef(loopEndLabel);

        // return value is always void
        Cgen.emitter.emitMove(CgenConstants.ACC, CgenConstants.ZERO);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(BlockNode node, String _unused) {
        /* TODO */
        //for each expression:
        for (ExpressionNode E : node.getExprs()) {
            //forceDest(E, CgenConstants.ACC);
            E.accept(this, CgenConstants.ACC);
        }
        return CgenConstants.ACC;
    }

    //Arithmetic Operations for Add, Subtract, Divide, Multiply
    @Override
    public String visit(IntBinopNode node, String _unused) {
        // evaluate e1 and store to a0
        node.getE1().accept(this, CgenConstants.ACC); //returns the register locations
        // push a0 to stack
        Cgen.emitter.emitPushAcc();

        // evaluate e2 and store to a0
        node.getE2().accept(this, CgenConstants.ACC);
        // duplicate the integer object at a0
        Cgen.emitter.emitJal(CgenConstants.OBJECT_COPY);
        // put address of e2's value in reg1
        Cgen.emitter.emitLoad(CgenConstants.regNames[1], 3, CgenConstants.ACC);

        // pop e1 to reg0
        Cgen.emitter.emitPop(CgenConstants.regNames[0]);
        // put address of e1's value in reg0
        Cgen.emitter.emitLoad(CgenConstants.regNames[0], 3, CgenConstants.regNames[0]);

        // store integer result of operation to reg0
        if (node instanceof PlusNode) {
            // add	$t1 $t1 $t2
            Cgen.emitter.emitAdd(CgenConstants.regNames[0], CgenConstants.regNames[0], CgenConstants.regNames[1]);
        } else if (node instanceof SubNode) {
            // sub	$t1 $t1 $t2
            Cgen.emitter.emitSub(CgenConstants.regNames[0], CgenConstants.regNames[0], CgenConstants.regNames[1]);
        } else if (node instanceof MulNode) {
            // mul	$t1 $t1 $t2
            Cgen.emitter.emitMul(CgenConstants.regNames[0], CgenConstants.regNames[0], CgenConstants.regNames[1]);
        } else if (node instanceof DivideNode) {
            // div	$t1 $t1 $t2
            Cgen.emitter.emitDiv(CgenConstants.regNames[0], CgenConstants.regNames[0], CgenConstants.regNames[1]);
        }

        // assign integer result back to integer object field
        Cgen.emitter.emitStore(CgenConstants.regNames[0], 3, CgenConstants.ACC);

        return CgenConstants.ACC;
    }

    //The calling convention for equality_test:
    //  INPUT: The two objects are passed in $t1 and $t2
    //  OUTPUT: Initial value of $a0, if the objects are equal
    //          Initial value of $a1, otherwise
    @Override
    public String visit(EqNode node, String _unused) {
        int label = CgenEnv.getFreshLabel();
        /* WIP */
        //cgen(e1)
        node.getE1().accept(this, CgenConstants.ACC);
        //push
        Cgen.emitter.emitPush(CgenConstants.ACC);
        //cgen(e2)
        node.getE2().accept(this, CgenConstants.ACC);
        //$t1 := top
        Cgen.emitter.emitTop(CgenConstants.T1);
        //pop
        Cgen.emitter.emitPop();
        //move $t2 $a0
        Cgen.emitter.emitMove(CgenConstants.T2, CgenConstants.ACC);
        // la	$a0 true
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);
        // beq $t1 $t2 label
        Cgen.emitter.emitBeq(CgenConstants.T1, CgenConstants.T2, label);
        // la	$a1 false
        Cgen.emitter.emitLoadBool(CgenConstants.A1, false);
        //jal	equality_test
        Cgen.emitter.emitEqualityTest();

        Cgen.emitter.emitLabelDef(label);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(LEqNode node, String _unused) {
        // evaluate the first expression
        node.getE1().accept(this, CgenConstants.ACC);

        // load the address of the value (offset 12) of the number into a temp register
        // Cgen.emitter.emitAddiu(CgenConstants.regNames[0], CgenConstants.ACC, 12);
        Cgen.emitter.emitLoad(CgenConstants.regNames[0], 3, CgenConstants.ACC);

        // push the expression's value's address to stack
        Cgen.emitter.emitPush(CgenConstants.regNames[0]);

        // evaluate the second expression
        node.getE2().accept(this, CgenConstants.ACC);

        // load the address of the value (offset 12) of the number into a second temp register
        // Cgen.emitter.emitAddiu(CgenConstants.regNames[1], CgenConstants.ACC, 12);
        Cgen.emitter.emitLoad(CgenConstants.regNames[1], 3, CgenConstants.ACC);

        // pop the first expression's value's address back to the first temp register
        Cgen.emitter.emitPop(CgenConstants.regNames[0]);

        int isLessThanLabel = CgenEnv.getFreshLabel();
        int endLabel = CgenEnv.getFreshLabel();

        // jump to isLessThanLabel if it is indeed less than or equal
        Cgen.emitter.emitBleq(CgenConstants.regNames[0], CgenConstants.regNames[1], isLessThanLabel);

        // is NOT less than or equal
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, false);
        Cgen.emitter.emitBranch(endLabel);

        // is indeed less than or equal
        Cgen.emitter.emitLabelDef(isLessThanLabel);
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);

        Cgen.emitter.emitLabelDef(endLabel);

        return CgenConstants.ACC;
    }

    @Override
    public String visit(LTNode node, String _unused) {

        // evaluate the first expression
        node.getE1().accept(this, CgenConstants.ACC);

        // load the address of the value (offset 12) of the number into a temp register
        // Cgen.emitter.emitAddiu(CgenConstants.regNames[0], CgenConstants.ACC, 12);
        Cgen.emitter.emitLoad(CgenConstants.regNames[0], 3, CgenConstants.ACC);

        // push the expression's value's address to stack
        Cgen.emitter.emitPush(CgenConstants.regNames[0]);

        // evaluate the second expression
        node.getE2().accept(this, CgenConstants.ACC);

        // load the address of the value (offset 12) of the number into a second temp register
        // Cgen.emitter.emitAddiu(CgenConstants.regNames[1], CgenConstants.ACC, 12);
        Cgen.emitter.emitLoad(CgenConstants.regNames[1], 3, CgenConstants.ACC);

        // pop the first expression's value's address back to the first temp register
        Cgen.emitter.emitPop(CgenConstants.regNames[0]);

        int isLessThanLabel = CgenEnv.getFreshLabel();
        int endLabel = CgenEnv.getFreshLabel();

        // jump to isLessThanLabel if it is indeed less than
        Cgen.emitter.emitBlt(CgenConstants.regNames[0], CgenConstants.regNames[1], isLessThanLabel);

        // is NOT less than
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, false);
        Cgen.emitter.emitBranch(endLabel);

        // is indeed less than
        Cgen.emitter.emitLabelDef(isLessThanLabel);
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);

        Cgen.emitter.emitLabelDef(endLabel);

        return CgenConstants.ACC;
    }

    @Override
    public String visit(NegNode node, String _unused) {
        /* TODO */
        //Cgen.emitter.emitDebugPrint("negnode test");
        //cgen(e1)
        // la	$a0 int_const0
        node.getE1().accept(this, CgenConstants.ACC);
        // jal	Object.copy
        Cgen.emitter.emitJal(CgenConstants.OBJECT_COPY);
        // lw	$t1 12($a0)
        Cgen.emitter.emitLoadVal(CgenConstants.T1, CgenConstants.ACC);
        // neg	$t1 $t1
        Cgen.emitter.emitNeg(CgenConstants.T1, CgenConstants.T1);
        // sw	$t1 12($a0)
        Cgen.emitter.emitStoreVal(CgenConstants.T1, CgenConstants.ACC);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(CompNode node, String _unused) {
        /* WIP */
        int label = CgenEnv.getFreshLabel();
        //cgen(e1)
        // la	$a0 bool_const
        forceDest(node.getE1(), CgenConstants.ACC);

        // lw $t1 $a0
        Cgen.emitter.emitLoadVal(CgenConstants.T1, CgenConstants.ACC);

        // 	la	$a0 true
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);
        // beqz
        Cgen.emitter.emitBeqz(CgenConstants.T1, label);
        // //	la	$a0 false
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, false);
        //label:
        Cgen.emitter.emitLabelDef(label);
        return CgenConstants.ACC;
    }

    // Default implementation
    @Override
    public String visit(IntConstNode node, String _unused) {
        Cgen.emitter.emitLoadInt(CgenConstants.ACC, node.getVal());
        return CgenConstants.ACC;
    }

    @Override
    public String visit(BoolConstNode node, String _unused) {
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, node.getVal());
        return CgenConstants.ACC;
    }

    @Override
    public String visit(StringConstNode node, String _unused) {
        Cgen.emitter.emitLoadString(CgenConstants.ACC, node.getVal());
        return CgenConstants.ACC;
    }

    @Override
    public String visit(IsVoidNode node, String _unused) {
        int label = CgenEnv.getFreshLabel();
        /* WIP */
        forceDest(node.getE1(), CgenConstants.ACC);
        // move	$t1 $a0
        Cgen.emitter.emitMove(CgenConstants.T1, CgenConstants.ACC);
        // 	la	$a0 true
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);
        // beqz
        Cgen.emitter.emitBeqz(CgenConstants.T1, label);
        // //	la	$a0 false
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, false);
        Cgen.emitter.emitLabelDef(label);
        return CgenConstants.ACC;
    }

    // Default implementation
    @Override
    public String visit(ObjectNode node, String target) {
        String ref = env.vars.lookup(node.getName()).emitRef(target);
        if (ref == null) {
            Utilities.fatalError(
                    String.format("Cgen got null for object '%s', did you implement emitRef yet?", node.getName()));
        }
        return ref;
    }

    @Override
    public String visit(NoExpressionNode node, String _unused) {
        Utilities.fatalError("Cgen reached no expr.\n");
        return null;
    }

    // forceDest is a wrapper for the code functions that guarantees the
    // result will go in "target".  Since the destination register is always
    // the target, there's no need for a return value.

    // Default implementation
    private void forceDest(ExpressionNode e, String target) {
        String r = e.accept(this, target);
        Cgen.emitter.emitMove(target, r); //omitted if target = r.
    }

    // Helper for "e1 op e2"
    //
    // The contents of the register that holds e1 could change when
    // e2 is executed, so we need to save the result of the first computation.
    // This function:
    //   1) evaluates e1
    //   2) allocates a new var
    //   3) puts the result of e1 in that new var.
    //
    // The caller of storeOperand function should deallocate the new variable.

    // Default implementation
    private void storeOperand(Symbol temp_var, ExpressionNode e1) {
        //where will temp_var be allocated?
        int offset = env.getNextTempOffset();
        String dest = CgenConstants.getRegister(offset);
        if (dest == null) { //whoops, temp_var is going on the stack
            dest = CgenConstants.ACC;
        }
        String r_e1 = e1.accept(this, dest); //r_e1 <- e1, where hopefully r_e1=dest
        env.addLocal(temp_var);
        env.vars.lookup(temp_var).emitUpdate(r_e1);
    }
}
