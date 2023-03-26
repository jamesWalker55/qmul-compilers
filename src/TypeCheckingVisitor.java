import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;

import ast.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;

class ObjectMap {
    // an object map, maps [object name] => [object type]
    // may represent O or Oc depending on the context
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

    /** Try to get the name from the map. If not found, search in the default map */
    public Symbol get(Symbol name, ObjectMap defaultMap) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");
        Symbol type = map.get(name);
        if (type == null) {
            type = defaultMap.get(name);
        }
        return type;
    }

    /** Clone this object map to a new map */
    public ObjectMap clone() {
        return new ObjectMap((HashMap<Symbol, Symbol>) map.clone());
    }

    /**
     * Convenience method for cloning the object map then adding a new assignment to it.
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
    // A map from [method name] => [method information]
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
    // the name of the class this class inherits from
    Symbol parentClassName;
    // Oc - Object map of the class
    ObjectMap objectMap;
    // M - Method map of the class
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
                // map each feature node to its type declaration, returning a list of type declarations
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

    /** Return a copy of this context but replace the current class name with the given one */
    public MyContext with(Symbol newClass) {
        return new MyContext(newClass, objectMap.clone());
    }

    /** Return a copy of this context but replace the object map with the given one */
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
        ClassInfo info = new ClassInfo(TreeConstants.Object_);
        info.methodMap.put(TreeConstants.out_string, TreeConstants.SELF_TYPE, Arrays.asList(TreeConstants.Str));
        info.methodMap.put(TreeConstants.out_int, TreeConstants.SELF_TYPE, Arrays.asList(TreeConstants.Int));
        info.methodMap.put(TreeConstants.in_string, TreeConstants.Str, Arrays.asList());
        info.methodMap.put(TreeConstants.in_int, TreeConstants.Int, Arrays.asList());
        map.put(TreeConstants.IO, info);
    }

    private void addIntClassInfo() {
        ClassInfo info = new ClassInfo(TreeConstants.Object_);
        map.put(TreeConstants.Int, info);
    }

    private void addStringClassInfo() {
        ClassInfo info = new ClassInfo(TreeConstants.Object_);
        info.methodMap.put(TreeConstants.length, TreeConstants.Int, Arrays.asList());
        info.methodMap.put(TreeConstants.concat, TreeConstants.Str, Arrays.asList(TreeConstants.Str));
        info.methodMap.put(TreeConstants.substr, TreeConstants.Str, Arrays.asList(TreeConstants.Int, TreeConstants.Int));
        map.put(TreeConstants.Str, info);
    }

    private void addBoolClassInfo() {
        ClassInfo info = new ClassInfo(TreeConstants.Object_);
        map.put(TreeConstants.Bool, info);
    }

    //Should return ClassInfo but need Object incase null
    public ClassInfo get(Symbol name) {
        return map.get(name);
    }

    public ClassInfo put(Symbol name, ClassInfo info) {
        return map.put(name, info);
    }

    /**
     * Return a list of class names, representing the inheritance chain of the given class.
     * Will return null if the class inherits from a non-existent class.
     */
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
                return null;
            }
            className = info.parentClassName;
        }
        return chain;
    }

    /** T' <= T */
    public boolean inheritsFrom(Symbol subType, Symbol parentType) {
        List<Symbol> subChain = inheritanceChain(subType);
        if (subChain == null) return false;

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
    Symbol filename;

    // go down the abstract syntax tree
    // then label each node with its type by proving the premises

    public String getFilename(){
        return filename.getName();
    }

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
            //if classname already definied or is of the basic types send error
            if (!Objects.isNull(classMap.get(name))){
                Utilities.semantError().println("class already definied");
                break;
            }

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
            return visit((AssignNode) node, ctx);
        } else if (node instanceof NewNode) {
            return visit((NewNode) node, ctx);
        } else if (node instanceof DispatchNode) {
            return visit((DispatchNode) node, ctx);
        } else if (node instanceof StaticDispatchNode) {
            return visit((StaticDispatchNode) node, ctx);
        } else if (node instanceof CondNode) {
            return visit((CondNode) node, ctx);
        } else if (node instanceof BlockNode) {
            return visit((BlockNode) node, ctx);
        } else if (node instanceof LetNode) {
            return visit((LetNode) node, ctx);
        } else if (node instanceof CaseNode) {
            return visit((CaseNode) node, ctx);
        } else if (node instanceof LoopNode) {
            return visit((LoopNode) node, ctx);
        } else if (node instanceof IsVoidNode) {
            return visit((IsVoidNode) node, ctx);
        } else if (node instanceof CompNode) {
            return visit((CompNode) node, ctx);
        } else if (node instanceof NegNode) {
            return visit((NegNode) node, ctx);
        } else if (node instanceof IntBinopNode) {
            return visit((IntBinopNode) node, ctx);
        } else if (node instanceof BoolBinopNode) {
            return visit((BoolBinopNode) node, ctx);
        } else if (node instanceof ObjectNode) {
            return visit((ObjectNode) node, ctx);
        } else if (node instanceof NoExpressionNode) {
            return visit((NoExpressionNode) node, ctx);
        }
        // basic types
        else if (node instanceof IntConstNode) {
            // [Int]
            node.setType(TreeConstants.Int);
            return TreeConstants.Int;
        } else if (node instanceof StringConstNode) {
            // [String]
            node.setType(TreeConstants.Str);
            return TreeConstants.Str;
        } else if (node instanceof BoolConstNode) {
            // [True] / [False]
            node.setType(TreeConstants.Bool);
            return TreeConstants.Bool;
        } else {
            // error for unknown class
            Utilities.semantError().println("ExpressionNode: Unknown node type: " + node.toString());
            return node.getType();
        }
    }

    @Override
    public Symbol visit(ClassNode node, MyContext _ctx) {
        this.filename = node.getFilename();
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
            node.setType(TreeConstants.No_type);
            return TreeConstants.No_type;
        } else if (node.getName().equals(TreeConstants.self)) {
            System.out.println("DETECTED A: self");
            node.setType(TreeConstants.SELF_TYPE);
            return type;
        } else {
            node.setType(type);
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
            node.setType(TreeConstants.No_type);
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

    // [Dispatch]
    @Override
    public Symbol visit(DispatchNode node, MyContext ctx) {
        // T0'
        Symbol exprType = visit(node.getExpr(), ctx);
        if (exprType.equals(TreeConstants.SELF_TYPE)) {
            exprType = ctx.currentClass;
        }

        // T0 ... Tn
        List<Symbol> actualTypes = new ArrayList<>();
        for (ExpressionNode exprNode : node.getActuals()) {
            actualTypes.add(visit(exprNode, ctx));
        }

        MethodInfo methodInfo = classMap.get(exprType).methodMap.get(node.getName());

        // T0' ... Tn'
        List<Symbol> formalTypes = methodInfo.signature;
        if (actualTypes.size() != formalTypes.size()) {
            Utilities.semantError().println("DispatchNode: Number of arguments to " + exprType.getName() + "#" + node.getName() + " differs from signature: " + actualTypes.size() + " != " + formalTypes.size());
        } else {
            for (int i = 0; i < formalTypes.size(); i++) {
                Symbol actual = actualTypes.get(i);
                Symbol formal = formalTypes.get(i);
                if (!classMap.inheritsFrom(actual, formal)) {
                    Utilities.semantError(this.filename, node).println(
                        "In call of method "+node.getName()+", type "+actual.getName()+" of parameter b does not conform to declared type "+formal.getName()+"."
                        );
                }
            }
        }

        // T n+1
        Symbol returnType;
        if (methodInfo.returnType.equals(TreeConstants.SELF_TYPE)) {
            returnType = ctx.currentClass;
        } else {
            returnType = methodInfo.returnType;
        }
        node.setType(returnType);
        return returnType;
    }


    // [If]
    public Symbol join(Symbol A, Symbol B, MyContext ctx){
        if (A.equals(B)){
            return A;
        }
        else if (A.equals(TreeConstants.SELF_TYPE)){
            return join(ctx.currentClass, A, ctx);
        }
        else{
            return classMap.lub(A, B);
        }
    }
    public Symbol visit(CondNode node, MyContext ctx) {
        //e1 : Bool (if)
        if(!visit(node.getCond(), ctx).equals(TreeConstants.Bool)) {
            Utilities.semantError().println("CondNode: Invalid type for condition: ");
        }
        //e2: T2 (then)
        Symbol T2 = visit(node.getElseExpr(), ctx);
        //e3: T3 (else)
        Symbol T3 = visit(node.getThenExpr(), ctx);

        //if e1 then e2 else e3 fi :
        node.setType(join(T2, T3, ctx));
        return node.getType();
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

    // [Loop]
    @Override
    public Symbol visit(LoopNode node, MyContext ctx) {
        //if e1 is bool
        if(!visit(node.getCond(), ctx).equals(TreeConstants.Bool)){
            Utilities.semantError();
        }

        // e2
        visit(node.getBody(), ctx);

        node.setType(TreeConstants.Object_);
        return node.getType();
    }

    // [Isvoid]
    @Override
    public Symbol visit(IsVoidNode node, MyContext ctx) {
        System.out.print(node.getE1().getType());
        visit(node.getE1(), ctx);
        node.setType(TreeConstants.Bool);
        return node.getType();
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

    // [Compare]
    @Override
    public Symbol visit(BoolBinopNode node, MyContext ctx){
        if (node instanceof EqNode) {
            return visit((EqNode) node, ctx);
        }
        else{   //LE or LT
            if(!visit(node.getE1(), ctx).equals(TreeConstants.Int)){
                Utilities.semantError();
            }
            if(!visit(node.getE2(), ctx).equals(TreeConstants.Int)){
                Utilities.semantError();
            }
            node.setType(TreeConstants.Bool);
            return node.getType();
        }
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
        ClassInfo currentClassInfo = classMap.get(ctx.currentClass);

        // This is Oc
        ObjectMap classObjectMap = currentClassInfo.objectMap;
        // Add Oc[SELF_TYPEc / self]
        // Use extend() to clone the map as well as put something in it
        // This is to avoid modifying the original Oc
        ObjectMap newObjectMap = classObjectMap.extend(TreeConstants.self, ctx.currentClass);
        // Add each Oc[Tn / xn]
        // Map is already cloned, can just use put() in the loop
        for (FormalNode formalNode : node.getFormals()) {
            newObjectMap.put(formalNode.getName(), formalNode.getType_decl());
        }

        // visit the expression using the new object map
        ExpressionNode expr = node.getExpr();
        Symbol exprType = visit(expr, ctx.with(newObjectMap));
        
        // check that T0' <= T0
        Symbol declaredType = node.getReturn_type();
        if (declaredType.equals(TreeConstants.SELF_TYPE)) {
            declaredType = ctx.currentClass;
        }
        if (!classMap.inheritsFrom(exprType, declaredType)) {
            Utilities.semantError(this.filename, node).println(
                "Inferred return type "+exprType.getName()+" of method "+node.getName()+" does not conform to declared return type "+declaredType.getName()+"."
                );
        }
        return declaredType;
    }
}
