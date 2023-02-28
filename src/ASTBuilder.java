import java.beans.Expression;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import ast.*;

//turn concrete/parse tree into AST
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
        //System.out.println(ctx.feature());
        if (ctx.feature() != null)
            for (CoolParser.FeatureContext f : ctx.feature()){
                c.add((FeatureNode) visitFeature(f));
            }
        return c;
    }

    @Override
    //Feature node is abstract: needs to work on subclasses MethodNode and AttributeNode
    public Tree visitFeature(CoolParser.FeatureContext ctx) {
        //System.out.println("run visitFeature");
        int ln = ctx.start.getLine();
        TerminalNode nameNode = ctx.OBJECT_IDENTIFIER();    //name of the object (method name or attribute name)
        TerminalNode typeNode = ctx.TYPE_IDENTIFIER();      //name of type (return type or attribute type)
        
        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        Symbol return_type = StringTable.idtable.get(typeNode.getSymbol().getText());

        ExpressionNode body;
        if (ctx.expr() != null){
            body = (ExpressionNode) visitExpr(ctx.expr()); //this only works if there is a valid expr
        }
        else{
            //if there is an empty method, there will be a syntax error instead
            //this code is for initialization (same as assignment of default value)
            //set expression to the default value
            //body = (ExpressionNode) new IntConstNode(ln, val);
            body = (ExpressionNode) new NoExpressionNode(ln);
        }

        List<FormalNode> formals = new LinkedList<FormalNode>();
        for (CoolParser.FormalContext f : ctx.formal())
            formals.add((FormalNode) visitFormal(f));

        // List<TerminalNode> list = ctx.getTokens(ln);//get the list of tokens and determine which rule it follows
        // System.out.println(ctx.PAREN_OPEN() == null); //not a function therefore it is an assignment
        if (ctx.PAREN_OPEN() == null){
            if (ctx.ASSIGN() != null){ //if there is an assignment sign
                System.out.println("ctx.expr()");
            }
            return new AttributeNode(ln, name, return_type, body);
        }
        else{
            //return new MethodNode(ln, name, formals, return_type, body);
            return (FeatureNode) visitMethod(ctx);
        }
    }

    public Tree visitFormal(CoolParser.FormalContext ctx) {
        //System.out.println("run visitFormal");
        int ln = ctx.start.getLine();
        TerminalNode nameNode = ctx.TYPE_IDENTIFIER();

        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        Symbol type = StringTable.idtable.addString(nameNode.getSymbol().getText());
        FormalNode node = new FormalNode(ln, name, type);
        return node;
    }

    //need conditions for each of the different types of expressions
    //need a way of checking each rule of the grammar
    public Tree visitExpr(CoolParser.ExprContext ctx){
        //System.out.println("run visitExpr");
        int ln = ctx.start.getLine();

        //long if statement to check all the rules
        //these should be in order to match parser priority
        // | OBJECT_IDENTIFIER
        // | INT_LITERAL
        // | STRING_LITERAL
        // | BOOL_LITERAL
        ExpressionNode node;

        //MAKE SURE ALL ARE ELSE IF 
        //OBJECT_IDENTIFIER ASSIGN expr
        if (ctx.OBJECT_IDENTIFIER().size() == 1 && ctx.ASSIGN() != null)
        {
            node = (ExpressionNode) visitAssign(ctx);
        }
        else if (!ctx.OBJECT_IDENTIFIER().isEmpty()){ //if array is not empty
            TerminalNode tNode = ctx.OBJECT_IDENTIFIER(0);//assume only 1 object identifier for now

            Symbol val = StringTable.idtable.addString(tNode.getSymbol().getText());
            node = (ExpressionNode) new ObjectNode(ln, val);
        }
        else if (ctx.INT_LITERAL() != null){
            TerminalNode tNode = ctx.INT_LITERAL();

            Symbol val = StringTable.idtable.addString(tNode.getSymbol().getText());
            node = (ExpressionNode) new IntConstNode(ln, val);
        }
        //else if (ctx.BOOL_LITERAL() != null){
        else{
            TerminalNode boolNode = ctx.BOOL_LITERAL();
            boolean val;
            if (boolNode.getText().equals("true")){
                val = true;
            }
            else{
                val = false;
            }
            
            //testing boolean for now
            node = new BoolConstNode(ln, val);
        }
        
        return node;
    }

    public Tree visitAttribute(CoolParser.FeatureContext ctx){
        int ln = ctx.start.getLine();

        // List<TerminalNode> ns = ctx.getTokens(ln);
        // System.out.println(ns);

        TerminalNode nameNode = ctx.TYPE_IDENTIFIER();

        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        Symbol type = StringTable.idtable.addString(nameNode.getSymbol().getText());
        ExpressionNode body = (ExpressionNode) visit(ctx.expr());

        AttributeNode a = new AttributeNode(ln, name, type, body);
        return a;
    }
    
    //WIP
    public Tree visitMethod(CoolParser.FeatureContext ctx){
        int ln = ctx.start.getLine();
        TerminalNode nameNode = ctx.OBJECT_IDENTIFIER();    //name of the object (method name or attribute name)
        TerminalNode typeNode = ctx.TYPE_IDENTIFIER();      //name of type (return type or attribute type)
        
        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        Symbol return_type = StringTable.idtable.get(typeNode.getSymbol().getText());

        ExpressionNode body = (ExpressionNode) visitExpr(ctx.expr());

        List<FormalNode> formals = new LinkedList<FormalNode>();

        MethodNode node = new MethodNode(ln, name, formals, return_type, body);
        return node;
    }

    public Tree visitAssign(CoolParser.ExprContext ctx){
        int ln = ctx.start.getLine();
        TerminalNode nameNode = ctx.OBJECT_IDENTIFIER(0); //name of variable
        Symbol name = StringTable.idtable.addString(nameNode.getSymbol().getText());
        ExpressionNode body = (ExpressionNode) visitExpr(ctx.expr(0));

        AssignNode node = new AssignNode(ln, name, body);
        return node;
    }
}
