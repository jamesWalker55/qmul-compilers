import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import ast.*;

public class ASTBuilder extends CoolParserBaseVisitor<Tree> {

    @Override
    public Tree visitProgram(CoolParser.ProgramContext ctx) {

        ProgramNode p = new ProgramNode(ctx.getStart().getLine());
        for (CoolParser.CoolClassContext c:ctx.coolClass()) {
            p.add((ClassNode)visitCoolClass(c));
        }
        return p;
    }

    //turn class subtree into class AST
    @Override
    public Tree visitCoolClass(CoolParser.CoolClassContext ctx) {
        int ln = ctx.start.getLine();
        //int ln = 1;

        //List<TerminalNode> ns = ctx.TYPE_IDENTIFIER();
        TerminalNode nameNode = ctx.TYPE_IDENTIFIER().get(0);

        //Symbol is a tree
        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        //Symbol parent = StringTable.idtable.addString(ns.get(0).getParent().getText());
        Symbol filename = StringTable.idtable.addString(ctx.start.getTokenSource().getSourceName());

        Symbol parent;
        if (ctx.TYPE_IDENTIFIER().size() > 1) //if the context has more than 1 type identifier, get superclass its inheriting from
        {
            TerminalNode parentNode = ctx.TYPE_IDENTIFIER(1);
            parent = StringTable.idtable.addString(parentNode.getSymbol().getText());
        }
        else{
            parent = TreeConstants.Object_;
        }

        ClassNode c = new ClassNode(ln, name, parent, filename);
        System.out.println(ctx.feature());
        if (ctx.feature() != null)
            for (CoolParser.FeatureContext f : ctx.feature()){
                c.add((FeatureNode) visitFeature(f));
            }
        return c;
    }

    @Override
    public Tree visitFeature(CoolParser.FeatureContext ctx) {
        System.out.println("run visitFeature");
        int ln = ctx.start.getLine();
        TerminalNode nameNode = ctx.OBJECT_IDENTIFIER();    //name of the object (method name or attribute name)
        TerminalNode typeNode = ctx.TYPE_IDENTIFIER();      //name of type (return type or attribute type)
        
        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        Symbol return_type = StringTable.idtable.get(typeNode.getSymbol().getText());
        ExpressionNode body = (ExpressionNode) visit(ctx.expr());
        List<FormalNode> formals = new LinkedList<FormalNode>();
        System.out.println(ctx.formal());
        for (CoolParser.FormalContext f : ctx.formal())
            formals.add((FormalNode) visitFormal(f));
        return new MethodNode(ln, name, formals, return_type, body);
    }

    public Tree visitFormal(CoolParser.FormalContext ctx) {
        System.out.println("run visitFormal");
        int ln = ctx.start.getLine();
        TerminalNode nameNode = ctx.TYPE_IDENTIFIER();

        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        Symbol type = StringTable.idtable.addString(nameNode.getSymbol().getText());
        FormalNode f = new FormalNode(ln, name, type);
        return f;
    }



    // public Tree visitAttribute(CoolParser.FeatureContext ctx){
    //     int ln = ctx.start.getLine();

    //     List<TerminalNode> ns = ctx.getTokens(ln);
    //     System.out.println(ns);

    //     Symbol name;
    //     Symbol type;
    //     ExpressionNode init;

    //     AttributeNode a = new AttributeNode(ln, name, type, init);
    //     return a;
    // }
    //public Tree visitMethod(CoolParser.FeatureContext ctx){}
}
