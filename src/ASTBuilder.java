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
        if (ctx.ASSIGN().size() == 1 && ctx.COLON().size() == 0) {
            return exprAssign(ctx);
        } else if (ctx.DOT() != null) {
            return exprStaticDispatch(ctx);
        } else if (ctx.PAREN_OPEN() != null && ctx.OBJECT_IDENTIFIER().size() == 1) {
            return exprDispatch(ctx);
        } else if (ctx.IF() != null) {
            return exprIf(ctx);
        } else if (ctx.WHILE() != null) {
            return exprWhile(ctx);
        } else if (ctx.BRACE_OPEN() != null) {
            return exprBlock(ctx);
        } else if (ctx.LET() != null) {
            return exprLet(ctx);
        } else if (ctx.CASE() != null) {
            return exprCase(ctx);
        } else if (ctx.NEW() != null) {
            return exprNew(ctx);
        } else if (ctx.ISVOID() != null) {
            return exprIsVoid(ctx);
        } else if (ctx.ADD() != null) {
            return exprAdd(ctx);
        } else if (ctx.SUB() != null) {
            return exprSub(ctx);
        } else if (ctx.MUL() != null) {
            return exprMul(ctx);
        } else if (ctx.DIV() != null) {
            return exprDiv(ctx);
        } else if (ctx.TILDE() != null) {
            return exprTilde(ctx);
        } else if (ctx.LT() != null) {
            return exprLT(ctx);
        } else if (ctx.LE() != null) {
            return exprLE(ctx);
        } else if (ctx.EQUAL() != null) {
            return exprEqual(ctx);
        } else if (ctx.NOT() != null) {
            return exprNot(ctx);
        } else if (ctx.PAREN_OPEN() != null) {
            return exprParen(ctx);
        } else if (ctx.OBJECT_IDENTIFIER().size() == 1) {
            return exprIdentifier(ctx);
        } else if (ctx.INT_LITERAL() != null) {
            return exprInteger(ctx);
        } else if (ctx.STRING_LITERAL() != null) {
            return exprString(ctx);
        } else if (ctx.BOOL_LITERAL() != null) {
            return exprBool(ctx);
        } else {
            assert (false); // unreachable
            return null;
        }
    }

    private Tree exprAssign(CoolParser.ExprContext ctx) {
        String name = ctx.OBJECT_IDENTIFIER().get(0).getSymbol().getText();
        ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr(0));

        return new AssignNode(
                ctx.start.getLine(),
                StringTable.idtable.addString(name),
                expr);
    }

    private Tree exprStaticDispatch(CoolParser.ExprContext ctx) {
        String typeName = ctx.OBJECT_IDENTIFIER().get(0).getSymbol().getText();
        String name = ctx.OBJECT_IDENTIFIER().get(0).getSymbol().getText();
        ExpressionNode expr = (ExpressionNode) visitExpr(ctx.expr(0));
        List<ExpressionNode> actuals = ctx
                .expr()
                .stream()
                .skip(1)
                .map(x -> (ExpressionNode) visitExpr(x))
                .collect(Collectors.toList());

        return new StaticDispatchNode(
                ctx.start.getLine(),
                expr,
                StringTable.idtable.addString(typeName),
                StringTable.idtable.addString(name),
                actuals);
    }

    private Tree exprDispatch(CoolParser.ExprContext ctx) {
        String name = ctx.OBJECT_IDENTIFIER().get(0).getSymbol().getText();
        ExpressionNode expr = (ExpressionNode) new ObjectNode(
                ctx.start.getLine(),
                StringTable.idtable.addString("self"));
        List<ExpressionNode> actuals = ctx
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

    private Tree exprIf(CoolParser.ExprContext ctx) {
        ExpressionNode condExpr = (ExpressionNode) visitExpr(ctx.expr(0));
        ExpressionNode thenExpr = (ExpressionNode) visitExpr(ctx.expr(1));
        ExpressionNode elseExpr = (ExpressionNode) visitExpr(ctx.expr(2));

        return new CondNode(
                ctx.start.getLine(),
                condExpr,
                thenExpr,
                elseExpr);
    }

    private Tree exprWhile(CoolParser.ExprContext ctx) {
        ExpressionNode condExpr = (ExpressionNode) visitExpr(ctx.expr(0));
        ExpressionNode bodyExpr = (ExpressionNode) visitExpr(ctx.expr(1));

        return new LoopNode(
                ctx.start.getLine(),
                condExpr,
                bodyExpr);
    }

    private Tree exprBlock(CoolParser.ExprContext ctx) {
        List<ExpressionNode> exprs = ctx
                .expr()
                .stream()
                .map(x -> (ExpressionNode) visitExpr(x))
                .collect(Collectors.toList());

        return new BlockNode(
                ctx.start.getLine(),
                exprs);
    }

    private Tree exprLet(CoolParser.ExprContext ctx) {
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
            String typeName = ((TerminalNode) ctx.getChild(objChildIndex + 2)).getSymbol().getText();

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

    private Tree exprCase(CoolParser.ExprContext ctx) {
        Interval ctxRange = ctx.getSourceInterval();

        List<BranchNode> cases = new ArrayList<BranchNode>();

        // iterate through identifiers in reverse order
        for (TerminalNode objIdentifier : ctx.OBJECT_IDENTIFIER()) {
            // calculate index of this token within the current context
            Interval objRange = objIdentifier.getSourceInterval();
            int objChildIndex = objRange.a - ctxRange.a;

            int lineNumber = objIdentifier.getSymbol().getLine();
            String name = objIdentifier.getSymbol().getText();
            String typeName = ((TerminalNode) ctx.getChild(objChildIndex + 2)).getSymbol().getText();
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

    private Tree exprNew(CoolParser.ExprContext ctx) {
        String name = ctx.TYPE_IDENTIFIER(0).getSymbol().getText();
        return new NewNode(ctx.start.getLine(), StringTable.idtable.addString(name));
    }

    private Tree exprIsVoid(CoolParser.ExprContext ctx) {
        ExpressionNode expr = (ExpressionNode) visitExpr((CoolParser.ExprContext) ctx.expr(0));
        return new IsVoidNode(ctx.start.getLine(), expr);
    }

    private Tree exprAdd(CoolParser.ExprContext ctx) {
        return new PlusNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprSub(CoolParser.ExprContext ctx) {
        return new SubNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprMul(CoolParser.ExprContext ctx) {
        return new MulNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprDiv(CoolParser.ExprContext ctx) {
        return new DivideNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprTilde(CoolParser.ExprContext ctx) {
        return new NegNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)));
    }

    private Tree exprLT(CoolParser.ExprContext ctx) {
        return new LTNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprLE(CoolParser.ExprContext ctx) {
        return new LEqNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprEqual(CoolParser.ExprContext ctx) {
        return new EqNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)),
                (ExpressionNode) visitExpr(ctx.expr(1)));
    }

    private Tree exprNot(CoolParser.ExprContext ctx) {
        return new CompNode(
                ctx.getStart().getLine(),
                (ExpressionNode) visitExpr(ctx.expr(0)));
    }

    private Tree exprParen(CoolParser.ExprContext ctx) {
        return (ExpressionNode) visitExpr(ctx.expr(0));
    }

    private Tree exprIdentifier(CoolParser.ExprContext ctx) {
        return new ObjectNode(
                ctx.getStart().getLine(),
                StringTable.idtable.addString(ctx.getText()));
    }

    private Tree exprInteger(CoolParser.ExprContext ctx) {
        return new IntConstNode(
                ctx.getStart().getLine(),
                StringTable.inttable.addString(ctx.getText()));
    }

    private Tree exprString(CoolParser.ExprContext ctx) {
        return new StringConstNode(
                ctx.getStart().getLine(),
                StringTable.stringtable.addString(ctx.getText()));
    }

    private Tree exprBool(CoolParser.ExprContext ctx) {
        return new BoolConstNode(
                ctx.getStart().getLine(),
                ctx.getText().substring(0, 1) == "t");
    }
}
