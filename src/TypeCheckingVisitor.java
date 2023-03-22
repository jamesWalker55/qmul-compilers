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

    /**
     * Try to get the name from the map. If not found, search in the default map
     * @param name
     * @param defaultMap
     * @return
     */
    public Symbol get(Symbol name, ObjectMap defaultMap) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        Symbol type = map.get(name);
        if (type == null) {
            type = defaultMap.get(name);
        }
        return type;
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

class MethodInfo {
    Symbol returnType;
    List<Symbol> signature;

    public MethodInfo(Symbol returnType, List<Symbol> signature) {
        this.returnType = returnType;
        this.signature = signature;
    }
}

class MethodMap {
    HashMap<Symbol, MethodInfo> map;

    public MethodMap() {
        map = new HashMap<>();
    }

    public MethodMap(HashMap<Symbol, MethodInfo> map) {
        this.map = map;
    }

    public void put(Symbol name, Symbol returnType, List<Symbol> signature) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        if (returnType == null)
            throw new IllegalArgumentException("Return type cannot be null");
        if (signature == null)
            throw new IllegalArgumentException("Signature cannot be null");
        map.put(name, new MethodInfo(returnType, signature));
    }

    public MethodInfo get(Symbol name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        return map.get(name);
    }

    public MethodMap clone() {
        return new MethodMap((HashMap<Symbol, MethodInfo>) map.clone());
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
                info.methodMap.put(methodNode.getName(), methodNode.getReturn_type(), signature);
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
    // C of "O, M, C"
    Symbol currentClass;
    // O of "O, M, C"
    ObjectMap objectMap;
    // M of "O, M, C" is in the ClassInfo object

    public MyContext(Symbol currentClass) {
        this.currentClass = currentClass;
        this.objectMap = new ObjectMap();
    }

    public MyContext(Symbol currentClass, ObjectMap objectMap) {
        this.currentClass = currentClass;
        this.objectMap = objectMap;
    }

    public MyContext with(Symbol newClass) {
        return new MyContext(newClass, objectMap.clone());
    }

    public MyContext with(ObjectMap newObjectMap) {
        return new MyContext(currentClass, newObjectMap);
    }
}

class ClassMap {
    HashMap<Symbol, ClassInfo> map;

    public ClassMap() {
        map = new HashMap<>();

        addObjectClassInfo();
        addIOClassInfo();
        addIntClassInfo();
        addStringClassInfo();
        addBoolClassInfo();
    }

    private void addObjectClassInfo() {
        ClassInfo info = new ClassInfo(null);
        info.methodMap.put(TreeConstants.cool_abort, TreeConstants.Object_, Arrays.asList());
        info.methodMap.put(TreeConstants.type_name, TreeConstants.Str, Arrays.asList());
        info.methodMap.put(TreeConstants.copy, TreeConstants.SELF_TYPE, Arrays.asList());
        map.put(TreeConstants.Object_, info);
    }

    private void addIOClassInfo() {
        ClassInfo info = new ClassInfo(null);
        info.methodMap.put(TreeConstants.out_string, TreeConstants.SELF_TYPE, Arrays.asList(TreeConstants.Str));
        info.methodMap.put(TreeConstants.out_int, TreeConstants.SELF_TYPE, Arrays.asList(TreeConstants.Int));
        info.methodMap.put(TreeConstants.in_string, TreeConstants.Str, Arrays.asList());
        info.methodMap.put(TreeConstants.in_int, TreeConstants.Int, Arrays.asList());
        map.put(TreeConstants.IO, info);
    }

    private void addIntClassInfo() {
        ClassInfo info = new ClassInfo(null);
        map.put(TreeConstants.Int, info);
    }

    private void addStringClassInfo() {
        ClassInfo info = new ClassInfo(null);
        info.methodMap.put(TreeConstants.length, TreeConstants.Int, Arrays.asList());
        info.methodMap.put(TreeConstants.concat, TreeConstants.Str, Arrays.asList(TreeConstants.Str));
        info.methodMap.put(TreeConstants.substr, TreeConstants.Str, Arrays.asList(TreeConstants.Int, TreeConstants.Int));
        map.put(TreeConstants.Str, info);
    }

    private void addBoolClassInfo() {
        ClassInfo info = new ClassInfo(null);
        map.put(TreeConstants.Bool, info);
    }

    public ClassInfo get(Symbol name) {
        return map.get(name);
    }

    public ClassInfo put(Symbol name, ClassInfo info) {
        return map.put(name, info);
    }

    public ArrayList<Symbol> inheritanceChain(Symbol className) {
        ArrayList<Symbol> chain = new ArrayList<>();
        while (className != null) {
            // add class name to beginning of chain
            chain.add(0, className);
            // lookup next parent
            ClassInfo info = map.get(className);
            if (info == null) {
                // parent class doesn't exist, error
                Utilities.semantError()
                        .println("ClassMap: Attempted to inherit from non-existent class: " + className.getName());
                break;
            }
            className = info.parentClassName;
        }
        return chain;
    }

    /** T' <= T */
    public boolean inheritsFrom(Symbol subType, Symbol parentType) {
        List<Symbol> subChain = inheritanceChain(subType);
        for (Symbol symbol : subChain) {
            if (symbol.equals(parentType)) {
                return true;
            }
        }
        return false;
    }

    public Symbol lub(Symbol x, Symbol y) {
        // if they are the same class, return either one
        if (x.equals(y)) {
            return x;
        }

        List<Symbol> xChain = inheritanceChain(x);
        List<Symbol> yChain = inheritanceChain(y);

        int i = 0;
        while (true) {
            x = xChain.get(i);
            y = yChain.get(i);
            if (x == null || y == null) break;
            if (!x.equals(y)) break;
            i += 1;
        }

        if (i == 0) {
            return null;
        } else {
            return xChain.get(i - 1);
        }
    }
}

public class TypeCheckingVisitor extends BaseVisitor<Symbol, MyContext> {

    ClassMap classMap;

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
        classMap = new ClassMap();
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
            Utilities.semantError().println("ExpressionNode: Unknown node type: " + node.toString());
        }
        return node.getType();
    }

    @Override
    public Symbol visit(ClassNode node, MyContext _ctx) {
        MyContext ctx = new MyContext(node.getName());
        return visit(node.getFeatures(), ctx);
    }

    // [Var]
    @Override
    public Symbol visit(ObjectNode node, MyContext ctx) {
        ClassInfo currentClassInfo = classMap.get(ctx.currentClass);
        Symbol type = ctx.objectMap.get(node.getName(), currentClassInfo.objectMap);
        if (type == null) {
            Utilities.semantError().println("ObjectNode: Identifier not yet defined.");
            return TreeConstants.No_type;
        } else {
            return type;
        }
    }

    // [ASSIGN]
    @Override
    public Symbol visit(AssignNode node, MyContext ctx) {
        ClassInfo currentClassInfo = classMap.get(ctx.currentClass);
        Symbol type = ctx.objectMap.get(node.getName(), currentClassInfo.objectMap);
        if (type == null) {
            Utilities.semantError().println("AssignNode: Identifier not yet defined.");
            return TreeConstants.No_type;
        }

        ctx.objectMap.put(node.getName(), type);

        // if type of e1 is not equal to T'
        // O, M, C |- e1 : T'
        Symbol exprType = visit((ExpressionNode) node.getExpr(), ctx); // e1's type

        // exprType not conforms to T
        if (!classMap.inheritsFrom(exprType, type)) {
            // error
            Utilities.semantError().println("AssignNode: Incompatible types: " + exprType.getName() + " <= " + type.getName());
        }

        node.setType(exprType);
        return exprType;
    }

    // [New]
    @Override
    public Symbol visit(NewNode node, MyContext ctx) {
        Symbol nodeType = node.getType_name();
        if (nodeType.equals(TreeConstants.SELF_TYPE)) {
            node.setType(ctx.currentClass);
            return ctx.currentClass;
        } else {
            node.setType(nodeType);
            return nodeType;
        }
    }

    // [Sequence]
    @Override
    public Symbol visit(BlockNode node, MyContext ctx) {
        Symbol lastExprType = null;
        ObjectMap nestedO = ctx.objectMap.clone();
        MyContext nestedCtx = ctx.with(nestedO);
        for (ExpressionNode expr : node.getExprs()) {
            lastExprType = visit(expr, nestedCtx);
        }
        node.setType(lastExprType);
        return lastExprType;
    }

    // [Not]
    @Override
    public Symbol visit(CompNode node, MyContext ctx) {
        // if e1 is of type bool
        Symbol nodeType = visit(node.getE1(), ctx);
        if (!nodeType.equals(TreeConstants.Bool)) {
            // error
            Utilities.semantError().println("CompNode: Invalid type for complement: " + nodeType.getName());
        }
        node.setType(TreeConstants.Bool);
        return TreeConstants.Bool;
    }

    // [Neg]
    @Override
    public Symbol visit(NegNode node, MyContext ctx) {
        Symbol nodeType = visit(node.getE1(), ctx);
        if (!nodeType.equals(TreeConstants.Int)) {
            // error
            Utilities.semantError().println("NegNode: Invalid type for negation: " + nodeType.getName());
        }
        node.setType(TreeConstants.Int);
        return node.getType();
    }

    // [Arith]
    @Override
    public Symbol visit(IntBinopNode node, MyContext ctx) {

        // if type is incorrect, send a semant error
        // O, M, C |- e1 : Int
        Symbol nodeType1 = visit(node.getE1(), ctx);
        if (!nodeType1.equals(TreeConstants.Int)) {
            // error format:
            // filename:ln: non-Int arguments: E1.Type + E2.Type
            Utilities.semantError().println("IntBinopNode: error here");
        }
        // O, M, C |- e2 : Int
        Symbol nodeType2 = visit(node.getE2(), ctx);
        if (!nodeType2.equals(TreeConstants.Int)) {
            Utilities.semantError().println("IntBinopNode: error here");
        }
        // op ∈ {∗, +, −, /}
        // operation is allowed and creates an int
        node.setType(TreeConstants.Int);
        return TreeConstants.Int;
    }

    // [Attr-Init] / [Attr-No-Init]
    @Override
    public Symbol visit(AttributeNode node, MyContext ctx) {
        ClassInfo currentClassInfo = classMap.get(ctx.currentClass);
        Symbol declaredType = node.getType_decl();
        ExpressionNode init = node.getInit();
        if (init instanceof NoExpressionNode) {
            // [Attr-No-Init]
            // no type checking needed
        } else {
            // [Attr-Init]
            ObjectMap newO = currentClassInfo.objectMap.extend(TreeConstants.self, ctx.currentClass);
            MyContext newCtx = ctx.with(newO);
            Symbol initType = visit(init, newCtx);
            if (!classMap.inheritsFrom(initType, declaredType)) {
                Utilities.semantError().println("AttributeNode: Initialisation expression does not have type: " + declaredType.getName());
            }
        }
        return declaredType;
    }

    // [Method]
    @Override
    public Symbol visit(MethodNode node, MyContext ctx) {
        // TODO: Page 22 of manual
        return visit(node.getExpr(), ctx);
    }
}
