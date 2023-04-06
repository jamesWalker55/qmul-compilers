import ast.AttributeNode;
import ast.FormalNode;
import ast.MethodNode;

class CgenEmitMethods extends CgenEmitVisitor {
    @Override
    Void visit(CgenNode v) {
        Cgen.emitter.emitDebugPrint("Visit");
        if (!v.basic()) {
            env = v.env;
            visit(v.getClassNode().getFeatures(),"");
        }
        super.visit(v);
        return null;
    }

    @Override
    public String visit(AttributeNode node, String target) {
        /* don't visit subexpressions of attribute declarations */
        return null;
    }

    @Override
    public String visit(MethodNode node, String target) {
        if (Flags.cgen_debug) System.err.println("  Coding method: " + node.getName().getName());
        int num_temps = env.methods.lookup(node.getName()).getTemps(); //each +1 to this increases the stack pointer size by 4 bytes 
        int num_formals = node.getFormals().size();
        env.vars.enterScope();

        // The first formal is at the highest address of the stack frame,
        // which is beyond all the temporaries (num_temporaries), the
        // callee-save area (3 words), and at the end of formals (num_formals-1).
        int next_formal = num_temps + 2 + num_formals;
        for (FormalNode f : node.getFormals()) {
            Cgen.LocalInfo linfo = new Cgen.LocalInfo(next_formal--);
            env.vars.addId(f.getName(), linfo);
        }
        env.vars.addId(TreeConstants.self, new Cgen.SelfInfo());

        Cgen.emitter.emitMethodRef(env.getClassname(), node.getName());
        Cgen.emitter.emitLabel();
        Cgen.emitter.emitDebugPrint("WE ARE HERE0");
        Cgen.emitter.prologue(num_temps); //this is where the MIPS for the method is
        Cgen.emitter.emitDebugPrint("WE ARE HERE1");
        if (Flags.cgen_Memmgr_Debug == Flags.GC_DEBUG)
            for (int i = num_formals - 1; i >= 0; i--)
            {
                Cgen.emitter.emitLoad(CgenConstants.A1, 3 + num_temps + i, CgenConstants.FP);
                Cgen.emitter.emitGCCheck(CgenConstants.A1);
            }
        //the line below is where the MIPS line is printed
        String result = node.getExpr().accept(this, CgenConstants.ACC); // CgenEmitMethods.visit(ExpressionNode, $a0)
        //Cgen.emitter.emitDebugPrint(result);
        Cgen.emitter.emitDebugPrint(node.getExpr().getType());
        Cgen.emitter.emitMove(CgenConstants.ACC, result); //moves result to accumulator
        Cgen.emitter.epilogue(num_temps,num_formals);
        Cgen.emitter.emitDebugPrint("WE ARE HERE3");
        env.vars.exitScope();
        return null;
    }
}
