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
        /* TODO */
        return CgenConstants.ACC;
    }

    @Override
    public String visit(CondNode node, String target) {
        /* WIP */
        // cgen(e1=e2)
        String E1E2 = node.getCond().accept(this, CgenConstants.ACC); //E1 Top, E2

        
        return CgenConstants.ACC;
    }

    @Override
    public String visit(LoopNode node, String target) {
        int loop_label = CgenEnv.getFreshLabel();
        /* TODO */
        return CgenConstants.ACC;
    }

    @Override
    public String visit(BlockNode node, String target) {
        /* TODO */
        return null;
    }

    
    @Override
    public String visit(PlusNode node, String data){
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

        // // add	$t2 $t1 $t2
        Cgen.emitter.emitAdd(CgenConstants.T1, CgenConstants.T1, CgenConstants.T2);

        // sw	$t1 12($a0) //value to address
        Cgen.emitter.emitStore(CgenConstants.T1, 3, CgenConstants.ACC);
        // pop
        Cgen.emitter.emitPop();
        return CgenConstants.ACC;
    }

    @Override
    public String visit(SubNode node, String data) {
        //cgen(e1)
        String E1 = node.getE1().accept(this, CgenConstants.ACC); //returns the register locations
        //push
        Cgen.emitter.emitPush(E1);//push E1 to top of stack
        //cgen(e2)
        String E2 = node.getE2().accept(this, CgenConstants.ACC);
        //$t1 := top
        Cgen.emitter.emitTop(CgenConstants.T1);

        //add $a0 $t1 $a0
        Cgen.emitter.emitSub(CgenConstants.ACC, CgenConstants.T1, CgenConstants.ACC);

        //pop
        Cgen.emitter.emitPop();
        return CgenConstants.ACC;    //return the result address
    }

    @Override
    public String visit(MulNode node, String data) {
        //cgen(e1)
        String E1 = node.getE1().accept(this, CgenConstants.ACC); //returns the register locations
        //push
        Cgen.emitter.emitPush(E1);//push E1 to top of stack
        //cgen(e2)
        String E2 = node.getE2().accept(this, CgenConstants.ACC);
        //$t1 := top
        Cgen.emitter.emitTop(CgenConstants.T1);

        //add $a0 $t1 $a0
        Cgen.emitter.emitMul(CgenConstants.ACC, CgenConstants.T1, CgenConstants.ACC);

        //pop
        Cgen.emitter.emitPop();
        return CgenConstants.ACC;    //return the result address
    }

    @Override
    public String visit(DivideNode node, String data) {
        //cgen(e1)
        String E1 = node.getE1().accept(this, CgenConstants.ACC); //returns the register locations
        //push
        Cgen.emitter.emitPush(E1);//push E1 to top of stack
        //cgen(e2)
        String E2 = node.getE2().accept(this, CgenConstants.ACC);
        //$t1 := top
        Cgen.emitter.emitTop(CgenConstants.T1);

        //add $a0 $t1 $a0
        Cgen.emitter.emitDiv(CgenConstants.ACC, CgenConstants.T1, CgenConstants.ACC);

        //pop
        Cgen.emitter.emitPop();
        return CgenConstants.ACC;    //return the result address
    }

    //The calling convention for equality_test:
    //  INPUT: The two objects are passed in $t1 and $t2
    //  OUTPUT: Initial value of $a0, if the objects are equal
    //          Initial value of $a1, otherwise
    @Override
    public String visit(EqNode node, String target) {
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
        //beq $a0 $t1 true_branch
        Cgen.emitter.emitBeq(CgenConstants.ACC, CgenConstants.T1, 0);
        return CgenConstants.ACC;
    }

    @Override
    public String visit(LEqNode node, String data) {
        /* TODO */
        return null;
    }

    @Override
    public String visit(LTNode node, String data) {
        /* TODO */
        return null;
    }

    @Override
    public String visit(NegNode node, String target) {
        /* TODO */
        return null;
    }

    @Override
    public String visit(CompNode node, String target) {
        /* TODO */
        return null;
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
        /* TODO */
        return null;
    }

    @Override
    public String visit(ObjectNode node, String target) {
        return env.vars.lookup(node.getName()).emitRef(target);
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
