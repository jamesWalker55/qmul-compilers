import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;

import ast.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

class ObjectMap {
    HashMap<Symbol, Symbol> map;

    public ObjectMap() {
        map = new HashMap<>();
    }

    public ObjectMap(HashMap<Symbol, Symbol> objectMap) {
        this.map = objectMap;
    }

    public void put(Symbol name, Symbol type) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        if (type == null)
            throw new IllegalArgumentException("Type cannot be null");
        map.put(name, type);
    }

    public Symbol get(Symbol name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        return map.get(name);
    }

    public ObjectMap clone() {
        return new ObjectMap((HashMap<Symbol, Symbol>) map.clone());
    }

    /**
     * Clone the object map and add a new assignment to it.
     * The purpose of this is to handle rules like:
     * `Oc[SELF_TYPEc / self]` or `Oc[T1 / xn]`.
     */
    public ObjectMap extend(Symbol name, Symbol type) {
        ObjectMap omap = clone();
        omap.put(name, type);
        return omap;
    }
}

class MethodMap {
    HashMap<Symbol, List<Symbol>> map;

    public MethodMap() {
        map = new HashMap<>();
    }

    public MethodMap(HashMap<Symbol, List<Symbol>> map) {
        this.map = map;
    }

    public void put(Symbol name, List<Symbol> signature) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        if (signature == null)
            throw new IllegalArgumentException("Signature cannot be null");
        map.put(name, signature);
    }

    public List<Symbol> get(Symbol name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        return map.get(name);
    }

    public MethodMap clone() {
        return new MethodMap((HashMap<Symbol, List<Symbol>>) map.clone());
    }
}

class ClassInfo {
    Symbol parentClassName;
    ObjectMap objectMap;
    MethodMap methodMap;

    public ClassInfo(Symbol parentClassName) {
        this.parentClassName = parentClassName;
        objectMap = new ObjectMap();
        methodMap = new MethodMap();
    }

    static ClassInfo fromClassNode(ClassNode classNode) {
        ClassInfo info = new ClassInfo(classNode.getParent());

        for (FeatureNode featureNode : classNode.getFeatures()) {
            if (featureNode instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) featureNode;
                List<Symbol> signature = methodNode
                        .getFormals()
                        .stream()
                        .map(n -> n.getType_decl())
                        .collect(Collectors.toList());
                info.methodMap.put(methodNode.getName(), signature);
            } else if (featureNode instanceof AttributeNode) {
                AttributeNode attributeNode = (AttributeNode) featureNode;
                info.objectMap.put(attributeNode.getName(), attributeNode.getType_decl());
            } else {
                throw new IllegalArgumentException("Unknown feature node received.");
            }
        }

        return info;
    }
}

class MyContext {
    Symbol currentClass;
    ObjectMap objectMap;

    public MyContext(Symbol currentClass) {
        this.currentClass = currentClass;
        this.objectMap = new ObjectMap();
    }
}

class MyInheritanceGraph {
    // Symbol1 is the Object, Symbol2 is its parent
    private Stack<HashMap<Symbol, Symbol>> tbl = new Stack<HashMap<Symbol, Symbol>>();

    public MyInheritanceGraph() {
        enterScope();
        // Object is the root
        addId(TreeConstants.Object_, null);
        addId(TreeConstants.IO, TreeConstants.Object_);
        addId(TreeConstants.Int, TreeConstants.Object_);
        addId(TreeConstants.Str, TreeConstants.Object_);
        addId(TreeConstants.Bool, TreeConstants.Object_);
        // System.out.println(this.toString());
        // System.out.println(conformance(TreeConstants.Int, TreeConstants.Object_));
        // System.out.println(conformance(TreeConstants.Object_, TreeConstants.Int));
    }

    // if A ≤ B
    public boolean conformance(Symbol A, Symbol B) {
        if (A.getName().equals(B.getName())) {
            return true;
        } else if (lub(A, B).equals(B)) {
            // if A is a subclass, the lub is B
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<Symbol> getPath(Symbol A) {
        ArrayList<Symbol> listA = new ArrayList<Symbol>();
        while (A != null) {
            listA.add(0, A); // push to front of array list
            A = lookup(A);
        }
        // System.out.println(listA);
        return listA;
    }

    // least upper bounds
    public Symbol lub(Symbol A, Symbol B) {
        // go up to the top then keep going down until not the same
        if (A.getName() == B.getName()) {
            return A;
        }

        ArrayList<Symbol> listA = getPath(A);
        ArrayList<Symbol> listB = getPath(B);

        int i = 0;
        while (listA.get(i) == listB.get(i)) {
            if (i == listA.size() - 1 || i == listB.size() - 1) {
                break;
            }
            i++;
        }
        // System.out.println("same: "+ listA.get(i).getName());
        return listA.get(i);
    }

    public void enterScope() {
        tbl.push(new HashMap<Symbol, Symbol>());
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

    /**
     * Adds a new entry to the symbol table.
     *
     * @param id     the symbol
     * @param parent the ctx associated with id
     */
    public void addId(Symbol id, Symbol parent) {
        if (tbl.empty()) {
            Utilities.fatalError("addId: can't add a symbol without a scope.");
        }
        tbl.peek().put(id, parent);
        // System.out.println(this.toString());
    }

    /**
     * Looks up an item through all scopes of the symbol table. If
     * found it returns the associated information field, if not it
     * returns <code>null</code>.
     *
     * @param sym the symbol
     * @return the parent of sym, or null if not found
     */
    public Symbol lookup(Symbol sym) {
        if (tbl.empty()) {
            Utilities.fatalError("lookup: no scope in symbol table.");
        }
        // I break the abstraction here a bit by knowing that stack is
        // really a vector.
        for (int i = tbl.size() - 1; i >= 0; i--) {
            Symbol info = tbl.elementAt(i).get(sym);
            if (info != null)
                return info;
        }
        return null;
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

public class TypeCheckingVisitor extends BaseVisitor<Symbol, MyContext> {

    HashMap<Symbol, ClassInfo> classMap;

    // go down the abstract syntax tree
    // then label each node with its type by proving the premises

    @Override
    public Symbol visit(ProgramNode node, MyContext _ctx) {
        populateClassMap(node);

        for (ClassNode classNode : node.getClasses()) {
            MyContext ctx = new MyContext(classNode.getName());
            visit(classNode, ctx);
        }

        return null;
    }

    private void populateClassMap(ProgramNode node) {
        classMap = new HashMap<>();
        for (ClassNode classNode : node.getClasses()) {
            Symbol name = classNode.getName();
            ClassInfo info = ClassInfo.fromClassNode(classNode);
            classMap.put(name, info);
        }
    }

    @Override
    public Symbol visit(ExpressionNode node, MyContext ctx) {
        // System.out.println(node.toString());
        // check the expression's type
        // if it is a type set it
        // else, visit node
        // The rules shown in the manual:
        if (node instanceof AssignNode) {
            visit((AssignNode) node, ctx);
        } else if (node instanceof NewNode) {
            visit((NewNode) node, ctx);
        } else if (node instanceof DispatchNode) {
            visit((DispatchNode) node, ctx);
        } else if (node instanceof StaticDispatchNode) {
            visit((StaticDispatchNode) node, ctx);
        } else if (node instanceof CondNode) {
            visit((CondNode) node, ctx);
        } else if (node instanceof BlockNode) {
            visit((BlockNode) node, ctx);
        } else if (node instanceof LetNode) {
            visit((LetNode) node, ctx);
        } else if (node instanceof CaseNode) {
            visit((CaseNode) node, ctx);
        } else if (node instanceof LoopNode) {
            visit((LoopNode) node, ctx);
        } else if (node instanceof IsVoidNode) {
            visit((IsVoidNode) node, ctx);
        } else if (node instanceof BoolUnopNode) {
            visit((BoolUnopNode) node, ctx);
        } else if (node instanceof CompNode) {
            visit((NegNode) node, ctx);
        } else if (node instanceof IntBinopNode) {
            visit((IntBinopNode) node, ctx);
        } else if (node instanceof EqNode) {
            visit((EqNode) node, ctx);
        } else if (node instanceof ObjectNode) {
            visit((ObjectNode) node, ctx);
        } else if (node instanceof NoExpressionNode) {
            visit((NoExpressionNode) node, ctx);
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
            System.out.println("unknown");
        }
        return node.getType();
    }

    @Override // rule for Not
    public Symbol visit(CompNode node, MyContext ctx) {
        // if e1 is of type bool
        if (visit(node.getE1(), ctx).equals(TreeConstants.Bool)) {
            node.setType(TreeConstants.Bool);
        } else {
            // error
            Utilities.semantError().println("CompNode: ERROR");
        }
        return node.getType();
    }

    @Override
    public Symbol visit(NegNode node, MyContext ctx) {
        if (!visit(node.getE1(), ctx).equals(TreeConstants.Int)) {
            // error
        } else {
            node.setType(TreeConstants.Int);
        }
        return node.getType();
    }

    @Override
    // for {∗, +, −, /} operations
    public Symbol visit(IntBinopNode node, MyContext ctx) {

        // if type is incorrect, send a semant error
        // O, M, C |- e1 : Int
        if (!visit(node.getE1(), ctx).equals(TreeConstants.Int)) {
            // error format:
            // filename:ln: non-Int arguments: E1.Type + E2.Type
            Utilities.semantError().println("error here");
        }
        // O, M, C |- e2 : Int
        if (!visit(node.getE2(), ctx).equals(TreeConstants.Int)) {
            Utilities.semantError().println("error here");
            ;
        }
        // op ∈ {∗, +, −, /}
        // operation is allowed and creates an int
        node.setType(TreeConstants.Int);
        return node.getType();
    }

    @Override
    public Symbol visit(BlockNode node, MyContext ctx) {
        // last line of the block
        List<ExpressionNode> expressions = node.getExprs();
        // visit the last expression and get its type
        node.setType(visit((ExpressionNode) expressions.get(expressions.size() - 1), ctx));
        return visit(node.getExprs(), ctx);
    }

    @Override
    public Symbol visit(MethodNode node, MyContext ctx) {
        // TODO: Page 22 of manual
        return visit(node.getExpr(), ctx);
    }

    @Override
    public Symbol visit(NewNode node, MyContext ctx) {
        Symbol T = node.getType_name();
        if (T.equals(TreeConstants.SELF_TYPE)) {
            node.setType(T);
        } else {
            node.setType(T);
        }
        return node.getType();
    }

    @Override
    public Symbol visit(ObjectNode node, MyContext ctx) {
        // this needs to check the symbol ctx
        String name = node.getName().toString();
        // System.out.println("Object"+ name);
        TableData ctx = ctx.lookup(node.getName(), "var");
        if (ctx == null) {
            // Utilities.fatalError("cool error mate");
        }
        return node.getType();
    }

    @Override
    public Symbol visit(AttributeNode node, MyContext ctx) {
        // Var rule
        // setting the type to the listed type
        // e.g x: Int;
        // System.out.println("Attribute");
        // System.out.println(node.getName());
        Symbol name = node.getName();
        // System.out.println(visit(node.getInit(), ctx).getName());
        Symbol type = node.getType_decl();
        // System.out.println("type "+type.toString());

        // add to symbol ctx and inheritance graph
        // System.out.println("add "+name.getName()+" with type "+type.getName());
        ctx.addId(name, "var", new TableData(type));

        return visit((ExpressionNode) node.getInit(), ctx); // attribute node returns no type if expression is empty
    }

    @Override
    public Symbol visit(AssignNode node, MyContext ctx) {
        TableData ctx = ctx.lookup(node.getName(), "var");
        // O(Id) = T

        Symbol T = ctx.getType();

        // if type of e1 is not equal to T'
        // O, M, C |- e1 : T'

        Symbol identifierT = visit((ExpressionNode) node.getExpr(), ctx); // e1's type
        // identifierT not conforms to T
        if (!ctx.graph.conformance(identifierT, T)) {
            // error
            System.out.println("error msg here");
        }
        node.setType(identifierT);
        return node.getType();
    }
}
