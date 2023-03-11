import ast.IntBinopNode;
import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;


class MyContext {
    //this should have data about the context: O, M, C
    //Objects O
    //Methods M
    //Class C
    //currently unsure how context should be implemented/how it is used
}

public class TypeCheckingVisitor extends BaseVisitor<Symbol, MyContext> {

    //go down the abstract syntax tree
    //then label each node with its type by proving the premises

    @Override
    public Symbol visit(PlusNode node, MyContext data){

        //if type is incorrect, send a semant error
        //O, M, C |- e1 : Int
        if (!visit(node.getE1(), data).equals(TreeConstants.Int))
        {
            //error format:
            //filename:ln: non-Int arguments: E1.Type + E2.Type
            Utilities.semantError().println("error here");
        }
        //O, M, C |- e2 : Int
        if (!visit(node.getE2(), data).equals(TreeConstants.Int))
        {
            Utilities.semantError().println("error here");;
        }
        //op ∈ {∗, +, −, /}
        //operation is allowed and creats an int
        node.setType(TreeConstants.Int);
        return visit((IntBinopNode) node, data);
    }

    @Override
    public Symbol visit(ExpressionNode node, MyContext data){
        //check the expression's type
        //if it is a type set it
        //else, visit node
        //System.out.println(node instanceof IntConstNode);
        if (node instanceof IntConstNode){
            node.setType(TreeConstants.Int);
        }
        else if (node instanceof StringConstNode){
            node.setType(TreeConstants.Str);
        }
        else if (node instanceof StringConstNode){
            node.setType(TreeConstants.Str);
        }
        else if (node instanceof BoolConstNode){
            node.setType(TreeConstants.Bool);
        }
        else{
            //error for unknown class
        }

        return node.getType();
    }
}
