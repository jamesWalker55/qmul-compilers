import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

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
        } else {
            exprNode = (ExpressionNode) new NoExpressionNode(ctx.start.getLine());
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
        if (ctx instanceof CoolParser.DottedDispatchContext)
            return visitExpr((CoolParser.DottedDispatchContext) ctx);
        else if (ctx instanceof CoolParser.DispatchContext)
            return visitExpr((CoolParser.DispatchContext) ctx);
        else if (ctx instanceof CoolParser.TildeContext)
            return visitExpr((CoolParser.TildeContext) ctx);
        else if (ctx instanceof CoolParser.IsVoidContext)
            return visitExpr((CoolParser.IsVoidContext) ctx);
        else if (ctx instanceof CoolParser.MulOrDivContext)
            return visitExpr((CoolParser.MulOrDivContext) ctx);
        else if (ctx instanceof CoolParser.AddOrSubContext)
            return visitExpr((CoolParser.AddOrSubContext) ctx);
        else if (ctx instanceof CoolParser.ComparatorContext)
            return visitExpr((CoolParser.ComparatorContext) ctx);
        else if (ctx instanceof CoolParser.NotContext)
            return visitExpr((CoolParser.NotContext) ctx);
        else if (ctx instanceof CoolParser.AssignContext)
            return visitExpr((CoolParser.AssignContext) ctx);
        else if (ctx instanceof CoolParser.IfContext)
            return visitExpr((CoolParser.IfContext) ctx);
        else if (ctx instanceof CoolParser.WhileContext)
            return visitExpr((CoolParser.WhileContext) ctx);
        else if (ctx instanceof CoolParser.BlockContext)
            return visitExpr((CoolParser.BlockContext) ctx);
        else if (ctx instanceof CoolParser.LetContext)
            return visitExpr((CoolParser.LetContext) ctx);
        else if (ctx instanceof CoolParser.CaseContext)
            return visitExpr((CoolParser.CaseContext) ctx);
        else if (ctx instanceof CoolParser.NewContext)
            return visitExpr((CoolParser.NewContext) ctx);
        else if (ctx instanceof CoolParser.ParenContext)
            return visitExpr((CoolParser.ParenContext) ctx);
        else if (ctx instanceof CoolParser.ObjectIdentifierContext)
            return visitExpr((CoolParser.ObjectIdentifierContext) ctx);
        else if (ctx instanceof CoolParser.IntLiteralContext)
            return visitExpr((CoolParser.IntLiteralContext) ctx);
        else if (ctx instanceof CoolParser.StringLiteralContext)
            return visitExpr((CoolParser.StringLiteralContext) ctx);
        else if (ctx instanceof CoolParser.BoolLiteralContext)
            return visitExpr((CoolParser.BoolLiteralContext) ctx);
        else {
            throw new AssertionError("Unknown class for ctx");
        }
    }

    private Tree visitExpr(CoolParser.AssignContext ctx) {
        String name = ctx.OBJECT_IDENTIFIER().getSymbol().getText();
        ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr());

        return new AssignNode(
                ctx.start.getLine(),
                StringTable.idtable.addString(name),
                expr);
    }

    private Tree visitExpr(CoolParser.DottedDispatchContext ctx) {
        ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr(0));
        String name = ctx.OBJECT_IDENTIFIER().getSymbol().getText();
        List<ExpressionNode> actuals = ctx
                .expr()
                .stream()
                .skip(1)
                .map(x -> (ExpressionNode) visitExpr(x))
                .collect(Collectors.toList());

        if (ctx.AT() != null) {
            String typeName = ctx.TYPE_IDENTIFIER().getSymbol().getText();
            return new StaticDispatchNode(
                    ctx.start.getLine(),
                    expr,
                    StringTable.idtable.addString(typeName),
                    StringTable.idtable.addString(name),
                    actuals);
        } else {
            return new DispatchNode(
                    ctx.start.getLine(),
                    expr,
                    StringTable.idtable.addString(name),
                    actuals);
        }

    }

    private Tree visitExpr(CoolParser.DispatchContext ctx) {
        String name = ctx.OBJECT_IDENTIFIER().getSymbol().getText();
        ExpressionNode expr = null;
        List<ExpressionNode> actuals = null;
        expr = (ExpressionNode) new ObjectNode(
                ctx.start.getLine(),
                StringTable.idtable.addString("self"));
        actuals = ctx
                .expr()
                .stream()
                .map(x -> (ExpressionNode) visitExpr(x))
                .collect(Collectors.toList());

        return new DispatchNode(
                ctx.start.getLine(),
                expr,
                StringTable.idtable.addString(name),
                actuals);
    }

    private Tree visitExpr(CoolParser.IfContext ctx) {
        ExpressionNode condExpr = (ExpressionNode) visitExpr(ctx.expr(0));
        ExpressionNode thenExpr = (ExpressionNode) visitExpr(ctx.expr(1));
        ExpressionNode elseExpr = (ExpressionNode) visitExpr(ctx.expr(2));

        return new CondNode(
                ctx.start.getLine(),
                condExpr,
                thenExpr,
                elseExpr);
    }

    private Tree visitExpr(CoolParser.WhileContext ctx) {
        ExpressionNode condExpr = (ExpressionNode) visitExpr(ctx.expr(0));
        ExpressionNode bodyExpr = (ExpressionNode) visitExpr(ctx.expr(1));

        return new LoopNode(
                ctx.start.getLine(),
                condExpr,
                bodyExpr);
    }

    private Tree visitExpr(CoolParser.BlockContext ctx) {
        List<ExpressionNode> exprs = ctx
                .expr()
                .stream()
                .map(x -> (ExpressionNode) visitExpr(x))
                .collect(Collectors.toList());

        return new BlockNode(
                ctx.start.getLine(),
                exprs);
    }

    private Tree visitExpr(CoolParser.LetContext ctx) {
        Interval ctxRange = ctx.getSourceInterval();

        // body expression must be the last expression
        List<CoolParser.ExprContext> exprContexts = ctx.expr();
        ExpressionNode prevNode = (ExpressionNode) visitExpr(exprContexts.get(exprContexts.size() - 1));

        // iterate through identifiers in reverse order
        List<TerminalNode> objectIdentifiers = ctx.OBJECT_IDENTIFIER();
        for (int i = objectIdentifiers.size() - 1; i >= 0; i--) {
            TerminalNode objIdentifier = objectIdentifiers.get(i);

            // calculate index of this token within the current context
            Interval objRange = objIdentifier.getSourceInterval();
            int objChildIndex = objRange.a - ctxRange.a;

            String name = objIdentifier.getSymbol().getText();
            String typeName = ((TerminalNode) ctx.getChild(objChildIndex +
                    2)).getSymbol().getText();

            // determine if this LetNode has a initial expression
            TerminalNode tokenAfterTypeIdentifier = (TerminalNode) ctx.getChild(objChildIndex + 3);
            ExpressionNode initExpr = null;
            if (tokenAfterTypeIdentifier.getSymbol().getType() == CoolParser.ASSIGN) {
                // OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER ASSIGN expr
                initExpr = (ExpressionNode) visitExpr((CoolParser.ExprContext) ctx.getChild(objChildIndex + 4));
            } else {
                // OBJECT_IDENTIFIER COLON TYPE_IDENTIFIER
                // initExpr is empty, leave as null
                initExpr = new NoExpressionNode(objIdentifier.getSymbol().getLine());
            }

            ExpressionNode currentNode = new LetNode(
                    objIdentifier.getSymbol().getLine(),
                    StringTable.idtable.addString(name),
                    StringTable.idtable.addString(typeName),
                    initExpr,
                    prevNode);

            prevNode = currentNode;
        }

        return prevNode;
    }

    private Tree visitExpr(CoolParser.CaseContext ctx) {
        Interval ctxRange = ctx.getSourceInterval();

        List<BranchNode> cases = new ArrayList<BranchNode>();

        // iterate through identifiers in reverse order
        for (TerminalNode objIdentifier : ctx.OBJECT_IDENTIFIER()) {
            // calculate index of this token within the current context
            Interval objRange = objIdentifier.getSourceInterval();
            int objChildIndex = objRange.a - ctxRange.a;

            int lineNumber = objIdentifier.getSymbol().getLine();
            String name = objIdentifier.getSymbol().getText();
            String typeName = ((TerminalNode) ctx.getChild(objChildIndex +
                    2)).getSymbol().getText();
            ExpressionNode caseExpr = (ExpressionNode) visitExpr(
                    (CoolParser.ExprContext) ctx.getChild(objChildIndex + 4));

            cases.add(new BranchNode(
                    lineNumber,
                    StringTable.idtable.addString(name),
                    StringTable.idtable.addString(typeName),
                    caseExpr));
        }

        ExpressionNode expr = (ExpressionNode) visitExpr((CoolParser.ExprContext) ctx.expr(0));
        return new CaseNode(ctx.start.getLine(), expr, cases);
    }

    private Tree visitExpr(CoolParser.NewContext ctx) {
        String name = ctx.TYPE_IDENTIFIER().getSymbol().getText();
        return new NewNode(ctx.start.getLine(), StringTable.idtable.addString(name));
    }

    private Tree visitExpr(CoolParser.IsVoidContext ctx) {
        ExpressionNode expr = (ExpressionNode) visitExpr((CoolParser.ExprContext) ctx.expr());
        return new IsVoidNode(ctx.start.getLine(), expr);
    }

    private Tree visitExpr(CoolParser.AddOrSubContext ctx) {
        if (ctx.ADD() != null) {
            return new PlusNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else if (ctx.SUB() != null) {
            return new SubNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else {
            throw new AssertionError("Unknown comparator in ctx: " + ctx.getText());
        }
    }

    private Tree visitExpr(CoolParser.MulOrDivContext ctx) {
        if (ctx.MUL() != null) {
            return new MulNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else if (ctx.DIV() != null) {
            return new DivideNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else {
            throw new AssertionError("Unknown comparator in ctx: " + ctx.getText());
        }
    }

    private Tree visitExpr(CoolParser.TildeContext ctx) {
        return new NegNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr()));
    }

    private Tree visitExpr(CoolParser.ComparatorContext ctx) {
        if (ctx.LT() != null) {
            return new LTNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else if (ctx.LE() != null) {
            return new LEqNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else if (ctx.EQUAL() != null) {
            return new EqNode(
                    ctx.getStart().getLine(),
                    (ExpressionNode) visitExpr(ctx.expr(0)),
                    (ExpressionNode) visitExpr(ctx.expr(1)));
        } else {
            throw new AssertionError("Unknown comparator in ctx: " + ctx.getText());
        }
    }

    private Tree visitExpr(CoolParser.NotContext ctx) {
        return new CompNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr()));
    }

    private Tree visitExpr(CoolParser.ParenContext ctx) {
        return (ExpressionNode) visitExpr(ctx.expr());
    }

    private Tree visitExpr(CoolParser.ObjectIdentifierContext ctx) {
        return new ObjectNode(
                ctx.getStart().getLine(),
                StringTable.idtable.addString(ctx.getText()));
    }

    private Tree visitExpr(CoolParser.IntLiteralContext ctx) {
        return new IntConstNode(
                ctx.getStart().getLine(),
                StringTable.inttable.addString(ctx.getText()));
    }

    private Tree visitExpr(CoolParser.StringLiteralContext ctx) {
        return new StringConstNode(
                ctx.getStart().getLine(),
                StringTable.stringtable.addString(ctx.getText()));
    }

    private Tree visitExpr(CoolParser.BoolLiteralContext ctx) {
        return new BoolConstNode(
                ctx.getStart().getLine(),
                ctx.getText().substring(0, 1).equals("t"));
    }
}
