import ast.IntBinopNode;
import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;


class MyContext {}

public class TypeCheckingVisitor extends BaseVisitor<Symbol, MyContext> {

    //go down the abstract syntax tree
    //then label each node with its type by proving the premises
    @Override
    public Symbol visit(PlusNode node, MyContext data){

        System.out.println("hihihhi");
        //if type is incorrect, send a semant error
        //O, M, C |- e1 : Int
        if (!visit(node.getE1(), data).equals(TreeConstants.Int))
        {
            Utilities.semantError().println("error here");
        }
        //O, M, C |- e2 : Int
        if (!visit(node.getE2(), data).equals(TreeConstants.Int))
        {
            Utilities.semantError().println("error here");;
        }
        //op ∈ {∗, +, −, /}
        //operation is allowed
        return visit((IntBinopNode) node, data);
    }

    @Override
    public Symbol visit(ExpressionNode node, MyContext data){
        node.setType(TreeConstants.Int); //set the node's type
        return node.getType();
    }
}
