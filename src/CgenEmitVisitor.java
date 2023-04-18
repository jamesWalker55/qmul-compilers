import ast.*;

import java.util.List;

public class CgenEmitVisitor extends CgenVisitor<String, String>{

    /* Emit code for expressions */
    CgenEnv env;

    //  target: if there is any choice, put the result here, but
    //  there are no guarantees.
    //  Use forceDest instead if you really care.
    //  Return value: the name of the register holding the result.
    //  Possibly the same as target.

    @Override
    public String visit(AssignNode node, String target) {
        Cgen.VarInfo lhs = env.vars.lookup(node.getName());
        String rhs_value = node.getExpr().accept(this, target);
        lhs.emitUpdate(rhs_value);
        return rhs_value;
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

    @Override
    public String visit(DispatchNode node, String target) {
        Symbol classname = node.getExpr().getType();
        if (classname == TreeConstants.SELF_TYPE)
            classname = env.getClassname();
        CgenNode c = Cgen.classTable.get(classname);
        Cgen.MethodInfo minfo = c.env.methods.lookup(node.getName());
        for (ExpressionNode e : node.getActuals()) {
            String r_actual = e.accept(this, CgenConstants.ACC);
            Cgen.emitter.emitPush(r_actual);
        }
        forceDest(node.getExpr(), CgenConstants.ACC);
        if (Flags.cgen_debug) System.err.println("    Dispatch to " + node.getName());
        int lab = CgenEnv.getFreshLabel();
        Cgen.emitter.emitBne(CgenConstants.ACC,CgenConstants.ZERO,lab);      // test for void
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
    public String visit(StaticDispatchNode node, String target) {
        /* TODO */
        //int label = env.getFreshLabel();
        //Cgen.emitter.emitLabelDef(label);
        //Cgen.emitter.emitDebugPrint("static dispatch");
        Symbol classname = node.getExpr().getType();
        if (classname == TreeConstants.SELF_TYPE)
            classname = env.getClassname();
        CgenNode c = Cgen.classTable.get(classname);
        Cgen.MethodInfo minfo = c.env.methods.lookup(node.getName());
        //load expression then push onto stack
        for (ExpressionNode e : node.getActuals()) {
            String r_actual = e.accept(this, CgenConstants.ACC);
            Cgen.emitter.emitPush(r_actual);
        }
        //Cgen.emitter.emitDebugPrint("test1");
        //forceDest(node.getExpr(), CgenConstants.ACC);
        //Cgen.emitter.emitDebugPrint("test2");
        if (Flags.cgen_debug) System.err.println("    Dispatch to " + node.getName());
        int lab = CgenEnv.getFreshLabel();
        Cgen.emitter.emitBne(CgenConstants.ACC,CgenConstants.ZERO,lab);      // test for void
        Cgen.emitter.emitLoadString(CgenConstants.ACC, env.getFilename());
        Cgen.emitter.emitLoadImm(CgenConstants.T1, node.getLineNumber());
        Cgen.emitter.emitDispatchAbort();
        Cgen.emitter.emitLabelDef(lab);
        //Cgen.emitter.emitDebugPrint("test3");
        //	la	$t1 Base_dispTab
        Cgen.emitter.emitLoadAddress(CgenConstants.T1, node.getType_name()+CgenConstants.DISPTAB_SUFFIX);
        //Cgen.emitter.emitDebugPrint("test4");
        Cgen.emitter.emitLoad(CgenConstants.T1, minfo.getOffset(), CgenConstants.T1);
        Cgen.emitter.emitJalr(CgenConstants.T1);
        //	lw	$s1 0($fp)
        Cgen.emitter.emitLoad(CgenConstants.regNames[0], 0, CgenConstants.FP);
        return CgenConstants.ACC;
    }

    // The cases are tested in the order
    // of most specific to least specific.  Since tags are assigned
    // in depth-first order with the root being assigned 0, tests for higher-numbered
    // classes should be emitted before lower-numbered classes.

    @Override
    public String visit(CaseNode node, String target) {
        int out_label = CgenEnv.getFreshLabel();

        String r_expr = node.getExpr().accept(this, CgenConstants.ACC);
        int lab = CgenEnv.getFreshLabel();
        Cgen.emitter.emitBne(r_expr,CgenConstants.ZERO,lab);      // test for void
        Cgen.emitter.emitLoadString(CgenConstants.ACC, env.getFilename());
        Cgen.emitter.emitLoadImm(CgenConstants.T1, node.getLineNumber());
        Cgen.emitter.emitCaseAbort2();
        Cgen.emitter.emitLabelDef(lab);
        Cgen.emitter.emitLoad(CgenConstants.T2, CgenConstants.TAG_OFFSET, r_expr);  // fetch the class tag

        for (int class_num = CgenEnv.getLastTag()-1; class_num >=0; class_num--)
            for(BranchNode b : node.getCases()) {
                int tag = Cgen.classTable.get(b.getType_decl()).env.getClassTag();
                if (class_num == tag) {
                    if (Flags.cgen_debug) System.err.println("    Coding case " + b.getType_decl());
                    // result is in ACC
                    // r_newvar is the value that we did the case on.  It will be bound to the new var.
                    String r_newvar = CgenConstants.ACC;

                    lab = CgenEnv.getFreshLabel();
                    CgenEnv downcast = Cgen.classTable.get(b.getType_decl()).env;
                    int class_tag = downcast.getClassTag();
                    int last_tag  = downcast.getMaxChildTag();

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

    @Override
    public String visit(LetNode node, String target) {
        // r_newvar is the register to which we think the new variable will be
        //  assigned.
        // r_newvar is null if register allocation is disabled or no regs availible.
        //r_init is the register that holds the result of the init expr.  We'd like
        //  r_init to be the same as r_newvar.
        String r_newvar = CgenConstants.getRegister(env.getNextTempOffset());

        String r_init = r_newvar;
        if (r_init == null){
            r_init = CgenConstants.ACC;
        }

        if (node.getInit() instanceof NoExpressionNode)
        {
            if (TreeConstants.Int == node.getType_decl())
            {
                Cgen.emitter.emitPartialLoadAddress(r_init);
                Cgen.emitter.codeRefInt(StringTable.inttable.get("0"));
                Cgen.emitter.emitNewline();
            }
            else if (TreeConstants.Str == node.getType_decl())
            {
                Cgen.emitter.emitPartialLoadAddress(r_init);
                Cgen.emitter.codeRefString(StringTable.stringtable.get(""));
                Cgen.emitter.emitNewline();
            }
            else if (TreeConstants.Bool == node.getType_decl())
            {
                Cgen.emitter.emitPartialLoadAddress(r_init);
                Cgen.emitter.codeRef(false);
                Cgen.emitter.emitNewline();
            }
            else
            {
                r_init = CgenConstants.ZERO;
            }
        }
        else
        {
            r_init = node.getInit().accept(this, r_init);
        }

        //Register r_init now holds the location of the value to which newvar should
        //be initialized.  Hopefully, r_init and newvar are one and the same, in
        //which case the code_update is a nop.
        env.addLocal(node.getIdentifier());
        Cgen.VarInfo newvar = env.vars.lookup(node.getIdentifier());
        newvar.emitUpdate(r_init);

        //test that r_newvar really contains the register to which newvar
        //was assigned.
        assert( CgenConstants.regEq(newvar.getRegister(), r_newvar) );

        String r_body = node.getBody().accept(this, target);
        env.removeLocal();
        return r_body;
    }

    @Override
    public String visit(NewNode node, String target) {
        //CgenNode c = Cgen.classTable.get(env.getClassname());
        /* TODO */
        Symbol name = node.getType_name();
        // la	$a0 IO_protObj
        Cgen.emitter.emitLoadAddress(CgenConstants.ACC, node.getType_name()+CgenConstants.PROTOBJ_SUFFIX);
        // jal	Object.copy
        Cgen.emitter.emitJal(CgenConstants.OBJECT_COPY);
        // jal	IO_init
        Cgen.emitter.emitJal(node.getType_name()+CgenConstants.CLASSINIT_SUFFIX);

        //Cgen.emitter.emitDebugPrint(node.getType_name().getName()+node.getType());
        //Cgen.emitter.emitDebugPrint(env.getClassname());
        if (name != TreeConstants.SELF_TYPE){
            //Cgen.emitter.emitDebugPrint(name);
            //Cgen.emitter.emitDebugPrint("test");
            // sw	$a0 12($s0)
            Cgen.emitter.emitStoreVal(CgenConstants.ACC, CgenConstants.SELF);
            return CgenConstants.ACC;
        }
        else{
            //	move	$s1 $a0
            Cgen.emitter.emitMove(CgenConstants.regNames[0], CgenConstants.ACC);
            //	move	$a0 $s1
            Cgen.emitter.emitMove(CgenConstants.ACC, CgenConstants.regNames[0]);
            return CgenConstants.regNames[0];
        }
    }
    

    @Override
    public String visit(CondNode node, String target) {
        /* WIP */
        // cgen(e1=e2)
        int labelThen = CgenEnv.getFreshLabel();
        //int labelElse = CgenEnv.getFreshLabel();
        int labelEndIf = CgenEnv.getFreshLabel();
        System.out.println(labelThen);
        //System.out.println(labelElse);
        String E1E2 = node.getCond().accept(this, CgenConstants.ACC);
        
        Cgen.emitter.emitLoadVal(CgenConstants.T1, CgenConstants.ACC);
        //Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);
        //Cgen.emitter.emitMove(CgenConstants.T2, CgenConstants.ACC);
        //beq $t2 $t1 true_branch
        //Cgen.emitter.emitBeq(CgenConstants.T2, CgenConstants.T1, labelThen);
        Cgen.emitter.emitBeqz(CgenConstants.T1, labelThen);

        // cgen(e3)
        String E3 = node.getThenExpr().accept(this, CgenConstants.ACC);
        //b end_if
        //Cgen.emitter.emitDebugPrint("	b	end_if");
        Cgen.emitter.emitBranch(labelEndIf);

        Cgen.emitter.emitLabelDef(labelThen);
        // cgen(e4)
        String E4 = node.getElseExpr().accept(this, CgenConstants.ACC);
        //Cgen.emitter.emitDebugPrint("end_if:");
        Cgen.emitter.emitLabelDef(labelEndIf);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(LoopNode node, String target) {
        int loop_label = CgenEnv.getFreshLabel();
        int end_label = CgenEnv.getFreshLabel();
        /* WIP */
        Cgen.emitter.emitLabelDef(loop_label); //start lavel
        //cgen(e1)
        forceDest(node.getCond(), CgenConstants.ACC);
        Cgen.emitter.emitMove(CgenConstants.T1, CgenConstants.ACC);
        //beq	$t1 $zero label1
        Cgen.emitter.emitBeq(CgenConstants.T1, CgenConstants.ZERO, loop_label);

        //cgen(e2)
        forceDest(node.getBody(), CgenConstants.ACC);

        //b	label0
        Cgen.emitter.emitBranch(loop_label);
        Cgen.emitter.emitLabelDef(end_label);
        //move	$a0 $zero
        Cgen.emitter.emitMove(CgenConstants.ACC, CgenConstants.ZERO);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(BlockNode node, String target) {
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
    public String visit(IntBinopNode node, String data){
        // Cgen(e1)
        // la	$a0 int_const0
        String E1 = node.getE1().accept(this, CgenConstants.ACC); //returns the register locations
        // push
        Cgen.emitter.emitPushAcc();
        // Cgen(e2)
        // la	$a0 int_const0
        String E2 = node.getE2().accept(this, CgenConstants.ACC);

        // jal	Object.copy
        Cgen.emitter.emitJal(CgenConstants.OBJECT_COPY);
        // loading values from addresses (value of address $a0 into $t2), (value of address $s1 into $t1)
        // top
        Cgen.emitter.emitTop(CgenConstants.regNames[0]);
        // lw	$t2 12($a0)
        Cgen.emitter.emitLoad(CgenConstants.T2, 3, CgenConstants.ACC);
        // lw	$t1 12($s1)
        Cgen.emitter.emitLoad(CgenConstants.T1, 3, CgenConstants.regNames[0]);

        if (node instanceof PlusNode){
            // add	$t1 $t1 $t2
            Cgen.emitter.emitAdd(CgenConstants.T1, CgenConstants.T1, CgenConstants.T2);
        }
        else if (node instanceof SubNode){
            // sub	$t1 $t1 $t2
            Cgen.emitter.emitSub(CgenConstants.T1, CgenConstants.T1, CgenConstants.T2);
        }
        else if (node instanceof MulNode){
            // mul	$t1 $t1 $t2
            Cgen.emitter.emitMul(CgenConstants.T1, CgenConstants.T1, CgenConstants.T2);
        }
        else if (node instanceof DivideNode){
            // div	$t1 $t1 $t2
            Cgen.emitter.emitDiv(CgenConstants.T1, CgenConstants.T1, CgenConstants.T2);
        }

        // sw	$t1 12($a0) //value to address
        Cgen.emitter.emitStore(CgenConstants.T1, 3, CgenConstants.ACC);
        // pop
        Cgen.emitter.emitPop();
        return CgenConstants.ACC;
    }

    //The calling convention for equality_test:
    //  INPUT: The two objects are passed in $t1 and $t2
    //  OUTPUT: Initial value of $a0, if the objects are equal
    //          Initial value of $a1, otherwise
    @Override
    public String visit(EqNode node, String target) {
        int label = CgenEnv.getFreshLabel();
        /* WIP */
        //cgen(e1)
        String E1 = node.getE1().accept(this, CgenConstants.ACC);
        //push
        Cgen.emitter.emitPush(CgenConstants.ACC);
        //cgen(e2)
        String E2 = node.getE2().accept(this, CgenConstants.ACC);
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
    public String visit(LEqNode node, String data) {
        int label = CgenEnv.getFreshLabel();
        /* WIP */
        //cgen(e1)
        String E1 = node.getE1().accept(this, CgenConstants.ACC);
        //push
        Cgen.emitter.emitPush(CgenConstants.ACC);
        //cgen(e2)
        String E2 = node.getE2().accept(this, CgenConstants.ACC);
        //$t1 := top
        Cgen.emitter.emitTop(CgenConstants.T1);
        //pop
        Cgen.emitter.emitPop();
        //move $t2 $a0
        Cgen.emitter.emitMove(CgenConstants.T2, CgenConstants.ACC);
        // la	$a0 true
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);
        // bleq $t1 $t2 label
        Cgen.emitter.emitBleq(CgenConstants.T1, CgenConstants.T2, label);
        // la	$a1 false
        Cgen.emitter.emitLoadBool(CgenConstants.A1, false);

        Cgen.emitter.emitLabelDef(label);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(LTNode node, String data) {
        int label = CgenEnv.getFreshLabel();
        /* WIP */
        //cgen(e1)
        String E1 = node.getE1().accept(this, CgenConstants.ACC);
        //push
        Cgen.emitter.emitPush(CgenConstants.ACC);
        //cgen(e2)
        String E2 = node.getE2().accept(this, CgenConstants.ACC);
        //$t1 := top
        Cgen.emitter.emitTop(CgenConstants.T1);
        //pop
        Cgen.emitter.emitPop();
        //move $t2 $a0
        Cgen.emitter.emitMove(CgenConstants.T2, CgenConstants.ACC);
        // la	$a0 true
        Cgen.emitter.emitLoadBool(CgenConstants.ACC, true);
        // blt $t1 $t2 label
        Cgen.emitter.emitBlt(CgenConstants.T1, CgenConstants.T2, label);
        // la	$a1 false
        Cgen.emitter.emitLoadBool(CgenConstants.A1, false);

        Cgen.emitter.emitLabelDef(label);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(NegNode node, String target) {
        /* TODO */
        Cgen.emitter.emitDebugPrint("negnode test");
        return null;
    }

    @Override
    public String visit(CompNode node, String target) {
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

    @Override
    public String visit(IntConstNode node, String target) {
        Cgen.emitter.emitLoadInt(target,node.getVal());
        return target;
    }

    @Override
    public String visit(BoolConstNode node, String target) {
        System.out.println("BoolConstNode: "+node.getVal());
        Cgen.emitter.emitLoadBool(target,node.getVal());
        return target;
    }

    @Override
    public String visit(StringConstNode node, String target) {
        Cgen.emitter.emitLoadString(target,node.getVal());
        return target;
    }

    @Override
    public String visit(IsVoidNode node, String target) {
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

    @Override
    public String visit(ObjectNode node, String target) {
        //if returns null, emit void (unsure if this is 100% correct)
        String result = env.vars.lookup(node.getName()).emitRef(target);

        if (result == null)
        {
            Cgen.emitter.emitLoadVal(CgenConstants.ACC, CgenConstants.SELF);
        }
        return CgenConstants.ACC;
    }

    @Override
    public String visit(NoExpressionNode node, String data) {
        Utilities.fatalError("Cgen reached no expr.\n");
        return null;
    }


    // forceDest is a wrapper for the code functions that guarantees the
    // result will go in "target".  Since the destination register is always
    // the target, there's no need for a return value.
    private void forceDest(ExpressionNode e, String target)
    {
        String r = e.accept(this, target);
        Cgen.emitter.emitMove(target, r);  //omitted if target = r.
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
    private void storeOperand(Symbol temp_var, ExpressionNode e1)
    {
        //where will temp_var be allocated?
        int offset = env.getNextTempOffset();
        String dest = CgenConstants.getRegister(offset);
        if (dest == null)
        { //whoops, temp_var is going on the stack
            dest = CgenConstants.ACC;
        }
        String r_e1 = e1.accept(this,dest); //r_e1 <- e1, where hopefully r_e1=dest
        env.addLocal(temp_var);
        env.vars.lookup(temp_var).emitUpdate(r_e1);
    }
}
