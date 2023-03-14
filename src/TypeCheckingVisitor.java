import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;


class MyContext {
    //this should have data about the context: O, M, C
    //Objects O "A function to assign type T to object"
    //Methods M 
    //Class C
    //currently unsure how context should be implemented/how it is used
    //the info for this is in Type Environments in the manual

    //needs a list of identifiers via a symbol table (provided class)
    //use a stack to push and pop when changing scope
    SymbolTable<TableData> table = new SymbolTable<TableData>();
    //Object[]
    // where index 0 = variable or method or class
    // where index 1 = type
    // where index 2 = properties

    //constructor
    public MyContext(){
        table.enterScope(); //enters the first scope
    }

    //useful symbol table functions:
    //addId(Symbol id, D info), enterScope(), exitScope()
    //this function should be used when defining new functions/variables

    public void addId(Symbol id, TableData info){ //unsure what type the data/info should be atm
        table.addId(id, info);
    }
    public TableData lookup(Symbol sym){
        return table.lookup(sym);
    }
    public Symbol lookupVariable(Symbol sym){
        return lookup(sym).type;
    }
}

class TableData{
    String kind;
    Symbol type;
    Object properties;
}

public class TypeCheckingVisitor extends BaseVisitor<Symbol, MyContext> {

    //go down the abstract syntax tree
    //then label each node with its type by proving the premises

    @Override
    public Symbol visit(ProgramNode node, MyContext data){
        //creates a new context when the program starts
        //this context is passed down
        data = new MyContext();
        return visit(node.getClasses(), data);
    }

    @Override
    //for {∗, +, −, /} operations
    public Symbol visit(IntBinopNode node, MyContext data){

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
        //operation is allowed and creates an int
        node.setType(TreeConstants.Int);
        return visit((IntBinopNode) node, data);
    }

    @Override
    public Symbol visit(ExpressionNode node, MyContext data){
        //check the expression's type
        //if it is a type set it
        //else, visit node
        //The rules shown in the manual:
        if (node instanceof DispatchNode){
            visit((DispatchNode) node, data);
        }
        else if (node instanceof StaticDispatchNode){
            visit((StaticDispatchNode) node, data);
        }
        else if (node instanceof CondNode){
            visit((CondNode) node, data);
        }
        else if (node instanceof LetNode){
            visit((LetNode) node, data);
        }
        else if (node instanceof CaseNode){
            visit((CaseNode) node, data);
        }
        else if (node instanceof LoopNode){
            visit((LoopNode) node, data);
        }
        else if (node instanceof IsVoidNode){
            visit((IsVoidNode) node, data);
        }
        else if (node instanceof BoolUnopNode){
            visit((BoolUnopNode) node, data);
        }
        else if (node instanceof CompNode){
            visit((NegNode) node, data);
        }
        else if (node instanceof IntBinopNode){
            visit((IntBinopNode) node, data);
        }
        else if (node instanceof EqNode){
            visit((EqNode) node, data);
        }
        else if (node instanceof ObjectNode){
            visit((ObjectNode) node, data);
        }
        //basic types
        else if (node instanceof IntConstNode){ 
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

    @Override
    public Symbol visit(ObjectNode node, MyContext data){
        //this needs to check the symbol table
        node.setType(data.lookupVariable(node.getName()));
        return node.getType();
    }

    @Override
    public Symbol visit(AssignNode node, MyContext data){
        //this needs to add symbols to the symbol table
    }
}
