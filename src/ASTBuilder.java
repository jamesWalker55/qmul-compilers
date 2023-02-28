import java.util.List;
import java.util.stream.Collectors;

import ast.*;

public class ASTBuilder extends CoolParserBaseVisitor<Tree> {
    @Override
    public Tree visitProgram(CoolParser.ProgramContext ctx) {
        ProgramNode p = new ProgramNode(ctx.getStart().getLine());
        for (CoolParser.CoolClassContext c : ctx.coolClass()) {
            p.add((ClassNode) visitCoolClass(c));
        }
        return p;
    }

    // turn class subtree into class AST
    @Override
    public Tree visitCoolClass(CoolParser.CoolClassContext ctx) {
        // name of the class
        String className = ctx.TYPE_IDENTIFIER().get(0).getSymbol().getText();

        // name of the parent class, defaults to "Object"
        String parentClassName = "Object";
        if (ctx.TYPE_IDENTIFIER().size() > 1) {
            parentClassName = ctx.TYPE_IDENTIFIER().get(1).getSymbol().getText();
        }

        // filename
        String filename = ctx.start.getTokenSource().getSourceName();

        ClassNode node = new ClassNode(
                ctx.start.getLine(),
                StringTable.idtable.addString(className),
                StringTable.idtable.addString(parentClassName),
                StringTable.idtable.addString(filename));

        for (CoolParser.FeatureContext f : ctx.feature()) {
            node.add((FeatureNode) visitFeature(f));
        }

        return node;
    }

    @Override
    public Tree visitFeature(CoolParser.FeatureContext ctx) {
        // the name
        String name = ctx.OBJECT_IDENTIFIER().getSymbol().getText();

        // attribute type / return type
        String typeName = ctx.TYPE_IDENTIFIER().getSymbol().getText();

        // expr: the initial value / method body
        ExpressionNode exprNode = null;
        if (ctx.expr() != null) {
            exprNode = (ExpressionNode) visitExpr(ctx.expr());
        }

        boolean isMethod = ctx.PAREN_OPEN() != null;

        if (isMethod) {
            List<FormalNode> formals = ctx
                    .formal()
                    .stream()
                    .map(x -> (FormalNode) visitFormal(x))
                    .collect(Collectors.toList());
            return new MethodNode(
                    ctx.start.getLine(),
                    StringTable.idtable.addString(name),
                    formals,
                    StringTable.idtable.addString(typeName),
                    exprNode);
        } else {
            return new AttributeNode(
                    ctx.start.getLine(),
                    StringTable.idtable.addString(name),
                    StringTable.idtable.addString(typeName),
                    exprNode);
        }
    }

    public Tree visitFormal(CoolParser.FormalContext ctx) {
        // the name
        String name = ctx.OBJECT_IDENTIFIER().getSymbol().getText();
        // attribute type
        String typeName = ctx.TYPE_IDENTIFIER().getSymbol().getText();

        return new FormalNode(
                ctx.start.getLine(),
                StringTable.idtable.addString(name),
                StringTable.idtable.addString(typeName));
    }

    public Tree visitExpr(CoolParser.ExprContext ctx) {
        return new IntConstNode(42, new Symbol("ass", 1));
    }

    // public Tree visitAttribute(CoolParser.FeatureContext ctx){
    // int ln = ctx.start.getLine();

    // List<TerminalNode> ns = ctx.getTokens(ln);
    // System.out.println(ns);

    // Symbol name;
    // Symbol type;
    // ExpressionNode init;

    // AttributeNode a = new AttributeNode(ln, name, type, init);
    // return a;
    // }
    // public Tree visitMethod(CoolParser.FeatureContext ctx){}
}
