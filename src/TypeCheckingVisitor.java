import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;

import ast.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;

class MySymbolTable {
    private Stack<HashMap<List<String>, TableData>> tbl = new Stack<HashMap<List<String>, TableData>>();

    public MySymbolTable() {
        enterScope();
    }

    /**
     * Enters a new scope. A scope must be entered before anything
     * can be added to the table.
     */
    public void enterScope() {
        tbl.push(new HashMap<List<String>, TableData>());
    }

    /**
     * Exits the most recently entered scope.
     */
    public void exitScope() {
        if (tbl.empty()) {
            Utilities.fatalError("existScope: can't remove scope from an empty symbol table.");
        }
        tbl.pop();
    }

    private List<String> createKey(Symbol id, String kind) {
        return Arrays.asList(id.getName(), kind);
    }

    /**
     * Adds a new entry to the symbol table.
     *
     * @param id   the symbol
     * @param info the data associated with id
     */
    public void addId(Symbol id, String kind, TableData info) {
        if (tbl.empty()) {
            Utilities.fatalError("addId: can't add a symbol without a scope.");
        }
        tbl.peek().put(createKey(id, kind), info);
    }

    /**
     * Looks up an item through all scopes of the symbol table. If
     * found it returns the associated information field, if not it
     * returns <code>null</code>.
     *
     * @param sym the symbol
     * @return the info associated with sym, or null if not found
     */
    public TableData lookup(Symbol id, String kind) {
        if (tbl.empty()) {
            Utilities.fatalError("lookup: no scope in symbol table.");
        }
        // I break the abstraction here a bit by knowing that stack is
        // really a vector.
        for (int i = tbl.size() - 1; i >= 0; i--) {
            TableData info = tbl.elementAt(i).get(createKey(id, kind));
            if (info != null)
                return info;
        }
        return null;
    }

    /**
     * Probes the symbol table. Check the top scope (only) for the
     * symbol <code>sym</code>. If found, return the information field.
     * If not return <code>null</code>.
     *
     * @param sym the symbol
     * @return the info associated with sym, or null if not found
     */
    public TableData probe(Symbol id, String kind) {
        if (tbl.empty()) {
            Utilities.fatalError("lookup: no scope in symbol table.");
        }
        return tbl.peek().get(createKey(id, kind));
    }

    /**
     * Gets the string representation of the symbol table.
     *
     * @return the string rep
     */
    public String toString() {
        String res = "";
        // I break the abstraction here a bit by knowing that stack is
        // really a vector...
        for (int i = tbl.size() - 1, j = 0; i >= 0; i--, j++) {
            res += "Scope " + j + ": " + tbl.elementAt(i) + "\n";
        }
        return res;
    }
}

class TableData {
    Symbol type;
    Object properties;

    public TableData(Symbol type) {
        this.type = type;
    }

    public TableData(Symbol type, Object properties) {
        this.type = type;
        this.properties = properties;
    }

    Object getProperties() {
        return properties;
    }

    public Symbol getType(){
        return this.type;
    }
}

public class TypeCheckingVisitor extends BaseVisitor<Symbol, MySymbolTable> {

    // go down the abstract syntax tree
    // then label each node with its type by proving the premises

    @Override
    public Symbol visit(ProgramNode node, MySymbolTable data) {
        // creates a new context when the program starts
        // this context is passed down

        data = new MySymbolTable();
        return visit(node.getClasses(), data);
    }

    @Override //rule for Not
    public Symbol visit(CompNode node, MySymbolTable table){
        //if e1 is of type bool
        if(!visit(node.getE1(), table).equals(TreeConstants.Bool)){
            //error
        }
        else{
            node.setType(TreeConstants.Bool);
        }
        return node.getType();
    }

    @Override
    public Symbol visit(NegNode node, MySymbolTable table){
        if(!visit(node.getE1(), table).equals(TreeConstants.Int)){
            //error
        }
        else{
            node.setType(TreeConstants.Int);
        }
        return node.getType();
    }

    @Override
    // for {∗, +, −, /} operations
    public Symbol visit(IntBinopNode node, MySymbolTable data) {

        // if type is incorrect, send a semant error
        // O, M, C |- e1 : Int
        if (!visit(node.getE1(), data).equals(TreeConstants.Int)) {
            // error format:
            // filename:ln: non-Int arguments: E1.Type + E2.Type
            Utilities.semantError().println("error here");
        }
        // O, M, C |- e2 : Int
        if (!visit(node.getE2(), data).equals(TreeConstants.Int)) {
            Utilities.semantError().println("error here");
            ;
        }
        // op ∈ {∗, +, −, /}
        // operation is allowed and creates an int
        node.setType(TreeConstants.Int);
        return node.getType();
    }

    @Override
    public Symbol visit(ExpressionNode node, MySymbolTable data) {
        // check the expression's type
        // if it is a type set it
        // else, visit node
        // The rules shown in the manual:
        if (node instanceof DispatchNode) {
            visit((DispatchNode) node, data);
        } else if (node instanceof StaticDispatchNode) {
            visit((StaticDispatchNode) node, data);
        } else if (node instanceof CondNode) {
            visit((CondNode) node, data);
        } else if (node instanceof LetNode) {
            visit((LetNode) node, data);
        } else if (node instanceof CaseNode) {
            visit((CaseNode) node, data);
        } else if (node instanceof LoopNode) {
            visit((LoopNode) node, data);
        } else if (node instanceof IsVoidNode) {
            visit((IsVoidNode) node, data);
        } else if (node instanceof BoolUnopNode) {
            visit((BoolUnopNode) node, data);
        } else if (node instanceof CompNode) {
            visit((NegNode) node, data);
        } else if (node instanceof IntBinopNode) {
            visit((IntBinopNode) node, data);
        } else if (node instanceof EqNode) {
            visit((EqNode) node, data);
        } else if (node instanceof ObjectNode) {
            visit((ObjectNode) node, data);
        }
        // basic types
        else if (node instanceof IntConstNode) {
            node.setType(TreeConstants.Int);
        } else if (node instanceof StringConstNode) {
            node.setType(TreeConstants.Str);
        } else if (node instanceof BoolConstNode) {
            node.setType(TreeConstants.Bool);
        } else {
            // error for unknown class
        }

        return node.getType();
    }

    @Override
    public Symbol visit(ClassNode node, MySymbolTable table){
        //add the current class to the context
        table.enterScope();
        table.addId(node.getName(), "class", new TableData(node.getName()));
        //System.out.println(table.lookup(node.getName(), "class").getType().getName());
        return visit(node.getFeatures(), table);
    }

    @Override
    public Symbol visit(ObjectNode node, MySymbolTable table) {
        // this needs to check the symbol table
        String name = node.getName().toString();
        System.out.println(name);
        TableData data = table.lookup(node.getName(), "variable");
        if (data == null) {
            //Utilities.fatalError("cool error mate");
        }
        return node.getType();
    }

    // @Override
    // public Symbol visit(AssignNode node, MySymbolTable table) {
    //     TableData data = table.lookup(node.getName(), "variable");
    //     //O(Id) = T
    //     if (!visit(node.getE1(), data).equals(TreeConstants.Int)) {
    //     Symbol T = data.getType();
    //     //if type of e1 is not equal to T'
    //     if (!E.equals(T2))

    //     //O, M, C |- e1 : T'
    //     Symbol T2 = visit((ExpressionNode)node.getExpr(), table);

    //     // this needs to add symbols to the symbol table
    //     table.addId(node.getName(), "variable", new TableData(node.getName()));
    //     return node.getName();
    // }
}
