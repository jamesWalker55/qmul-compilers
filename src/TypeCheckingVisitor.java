import ast.*; //imports all the node types from the ast
import ast.visitor.BaseVisitor;

import ast.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;

class Error {
    public static void semant(String format, Object... args) {
        Utilities.semantError().println(String.format(format, args));
    }
}

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
        info.methodMap.put(TreeConstants.copy, TreeConstants.self, Arrays.asList());
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

    // Lookup a method name on a given class, following inheritance
    // Will return null if method does not exist on the inherited class chain
    public MethodInfo lookupMethod(Symbol className, Symbol methodName) {
        ClassInfo info = get(className);
        while (info != null) {
            MethodInfo method = info.methodMap.get(methodName);
            if (method != null) {
                return method;
            }
            info = get(info.parentClassName);
        }

        // we searched through the entire chain and failed to find the method
        // method does not exist
        return null;
    }

    // Lookup an attribute name on a given class, following inheritance
    // Will return null if attribute does not exist on the inherited class chain
    public Symbol lookupObject(Symbol className, Symbol objectName) {
        ClassInfo info = get(className);
        while (info != null) {
            Symbol objectType = info.objectMap.get(objectName);
            if (objectType != null) {
                return objectType;
            }
            info = get(info.parentClassName);
        }

        // we searched through the entire chain and failed to find the attribute
        // attribute does not exist
        return null;
    }

    // Lookup an attribute name in the local map first, then the given class, following inheritance
    // Will return null if attribute does not exist locally or on the inherited class chain
    public Symbol lookupObject(ObjectMap localMap, Symbol className, Symbol objectName) {
        // First, search in local variables only
        Symbol type = localMap.get(objectName);
        // if not found, search in class inheritance chain
        if (type == null) {
            type = lookupObject(className, objectName);
        }
        return type;
    }

    public ClassInfo put(Symbol name, ClassInfo info) {
        // do error checking if it has a parent class
        if (info.parentClassName != null) {
            // cannot override attributes of inherited classes
            for (Map.Entry<Symbol,Symbol> entry : info.objectMap.map.entrySet()) {
                Boolean parentHasAttribute = lookupObject(info.parentClassName, entry.getKey()) != null;
                if (parentHasAttribute) {
                    Error.semant(
                        "ClassInfo: Attribute '%s' of '%s' conflicts attribute in '%s'",
                        entry.getKey(),
                        name,
                        info.parentClassName);
                }
            }
            // can override methods of inherited classes, but signature must be the same
            for (Map.Entry<Symbol,MethodInfo> entry : info.methodMap.map.entrySet()) {
                Symbol methodName = entry.getKey();
                MethodInfo method = entry.getValue();
                MethodInfo parentMethod = lookupMethod(info.parentClassName, methodName);
                if (parentMethod == null) continue;

                // check parameter types
                if (method.signature.size() != parentMethod.signature.size()) {
                    Error.semant(
                        "ClassInfo: Method '%s' of '%s' has different number of parameters than that in '%s'",
                        methodName,
                        name,
                        info.parentClassName);
                } else {
                    for (int i = 0; i < method.signature.size(); i++) {
                        Symbol a = method.signature.get(i);
                        Symbol b = parentMethod.signature.get(i);
                        if (!a.equals(b)) {
                            Error.semant(
                                "ClassInfo: Parameter #%d of overriden method '%s.%s' doesn't match definition in '%s'",
                                i,
                                name,
                                methodName,
                                info.parentClassName);
                        }
                    }
                }
            }
        }
        
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
                Error.semant(
                    "ClassMap: Attempted to inherit from non-existent class: %s",
                    className.getName());
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
            if (i >= xChain.size() || i >= yChain.size()) {
                break;
            }
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

        boolean mainExists = false;
        for (ClassNode classNode : node.getClasses()) {
            MyContext ctx = new MyContext(classNode.getName());
            visit(classNode, ctx);
            if (classNode.getName().equals(TreeConstants.Main)){
                mainExists = true;
            }
            //if classnode.parent not defined
            if (classMap.get(classNode.getParent()) == null ){
                Error.semant("Missing Class");
            } 
        }

        //if no main, send error
        if (mainExists == false){
            Error.semant("ProgramNode: No main class.");
        }

        return null;
    }

    private void populateClassMap(ProgramNode node) {
        classMap = new ClassMap();
        for (ClassNode classNode : node.getClasses()) {
            Symbol name = classNode.getName();
            //if classname already definied or is of the basic types send error
            if (!Objects.isNull(classMap.get(name))){
                Error.semant("class already definied");
                return;
            } else if(name.equals(TreeConstants.SELF_TYPE)){
                Error.semant("self in wrong place");
            }
            //if inherits from bool or int or string or selftype send error
            if (classNode.getParent().equals(TreeConstants.Bool)||
            classNode.getParent().equals(TreeConstants.Int)||
            classNode.getParent().equals(TreeConstants.Str)||
            classNode.getParent().equals(TreeConstants.SELF_TYPE)
            ){
                Error.semant("can't inherit from certain types");
            }
        

            ClassInfo info = ClassInfo.fromClassNode(classNode);
            classMap.put(name, info);
        }
    }

    @Override
    public Symbol visit(ExpressionNode node, MyContext ctx) {
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
            Error.semant("ExpressionNode: Unknown node type: %s", node.toString());
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
        if (node.getName().equals(TreeConstants.self)) {
            node.setType(TreeConstants.SELF_TYPE);
            return ctx.currentClass;
        }

        Symbol type = classMap.lookupObject(ctx.objectMap, ctx.currentClass, node.getName());
        if (type == null) {
            Error.semant("ObjectNode: Identifier not yet defined. %s", node.getName());
            node.setType(TreeConstants.No_type);
            return TreeConstants.No_type;
        } else {
            node.setType(type);
            return type;
        }
    }

    // [ASSIGN]
    @Override
    public Symbol visit(AssignNode node, MyContext ctx) {
        if(node.getName().equals(TreeConstants.self)){
            Error.semant("AssignNode: Assignment to 'self' is not allowed");
            return TreeConstants.No_type;
        }
        
        Symbol type = classMap.lookupObject(ctx.objectMap, ctx.currentClass, node.getName());
        if (type == null) {
            Error.semant("AssignNode: Identifier not yet defined.");
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
            Error.semant("AssignNode: Incompatible types: %s <= %s", exprType.getName(), type.getName());
        }

        node.setType(exprType);
        return exprType;
    }

    // [New]
    @Override
    public Symbol visit(NewNode node, MyContext ctx) {
        Symbol nodeType = node.getType_name();
        if (nodeType.equals(TreeConstants.SELF_TYPE)) {
            node.setType(TreeConstants.SELF_TYPE);
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
        // typecheck the expression with #visit()
        visit(node.getExpr(), ctx); // returns dont use SELF_TYPE
        Symbol exprType = node.getExpr().getType();
        if (exprType.equals(TreeConstants.SELF_TYPE)) {
            exprType = ctx.currentClass;
        }

        // T0 ... Tn
        List<Symbol> actualTypes = new ArrayList<>();
        for (ExpressionNode exprNode : node.getActuals()) {
            actualTypes.add(visit(exprNode, ctx));
        }

        MethodInfo methodInfo = classMap.lookupMethod(exprType, node.getName());
        if (methodInfo == null) {
            Error.semant("DispatchNode: Method %s does not exist on type %s", node.getName(), exprType);
            return TreeConstants.No_type;
        }

        // T0' ... Tn'
        List<Symbol> formalTypes = methodInfo.signature;
        if (actualTypes.size() != formalTypes.size()) {
            Error.semant(
                "DispatchNode: Number of arguments to %s#%s differs from signature: %s != %s",
                exprType.getName(),
                node.getName(),
                actualTypes.size(),
                formalTypes.size());
        } else {
            for (int i = 0; i < formalTypes.size(); i++) {
                Symbol actual = actualTypes.get(i);
                Symbol formal = formalTypes.get(i);
                if (!classMap.inheritsFrom(actual, formal)) {
                    Error.semant(
                        "DispatchNode: Parameter %d of %s#%s has incompatible types with signature: %s !<= %s",
                        i + 1,
                        exprType.getName(),
                        node.getName(),
                        actual.getName(),
                        formal.getName());
                }
            }
        }

        // T n+1
        Symbol returnType;
        if (methodInfo.returnType.equals(TreeConstants.self)) {
            returnType = exprType;
            node.setType(returnType);
        } else if (methodInfo.returnType.equals(TreeConstants.SELF_TYPE)) {
            returnType = exprType;
            node.setType(TreeConstants.SELF_TYPE);
        } else {
            returnType = methodInfo.returnType;
            node.setType(returnType);
        }

        return returnType;
    }

    // [StaticDispatch]
    @Override
    public Symbol visit(StaticDispatchNode node, MyContext ctx) {
        // T0'
        visit(node.getExpr(), ctx); //returns dont use SELF_TYPE
        Symbol exprType = node.getExpr().getType();
        if (exprType.equals(TreeConstants.SELF_TYPE)) {
            Error.semant("StaticDispatchNode: SELF_TYPE not allowed as expression");
            return TreeConstants.SELF_TYPE;
        }

        // T0 ... Tn
        List<Symbol> actualTypes = new ArrayList<>();
        for (ExpressionNode exprNode : node.getActuals()) {
            actualTypes.add(visit(exprNode, ctx));
        }

        //T0 conforms to T
        Symbol T = node.getType_name();
        if (!classMap.inheritsFrom(exprType, T)){
            Error.semant("StaticDispatchNode: Unknown error");
        }

        MethodInfo methodInfo = classMap.lookupMethod(T, node.getName());
        if (methodInfo == null) {
            Error.semant("DispatchNode: Method %s does not exist on type %s", node.getName(), T);
            return TreeConstants.No_type;
        }

        // T0' ... Tn'
        List<Symbol> formalTypes = methodInfo.signature;
        if (actualTypes.size() != formalTypes.size()) {
            Error.semant(
                "StaticDispatchNode: Number of arguments to %s#%s differs from signature: %s != %s",
                exprType.getName(),
                node.getName(),
                actualTypes.size(),
                formalTypes.size());
        } else {
            for (int i = 0; i < formalTypes.size(); i++) {
                Symbol actual = actualTypes.get(i);
                Symbol formal = formalTypes.get(i);
                if (!classMap.inheritsFrom(actual, formal)) {
                    Error.semant(
                        "StaticDispatchNode: Parameter %d of %s#%s has incompatible types with signature: %s !<= %s",
                        i + 1,
                        exprType.getName(),
                        node.getName(),
                        actual.getName(),
                        formal.getName());
                }
            }
        }

        // T n+1
        Symbol returnType;
        if (methodInfo.returnType.equals(TreeConstants.SELF_TYPE)) {
            returnType = exprType;
        } else {
            returnType = methodInfo.returnType;
        }
        node.setType(returnType);
        return returnType;
    }

    // [If]
    public Symbol join(MyContext ctx, Symbol... types){
        if (types.length == 0) {
            throw new IllegalArgumentException();
        }
        Symbol commonType = types[0];
        if (commonType.equals(TreeConstants.SELF_TYPE)) {
            commonType = ctx.currentClass;
        }
        for (int i = 1; i < types.length; i++) {
            Symbol type = types[i];
            if (type.equals(TreeConstants.SELF_TYPE)) {
                type = ctx.currentClass;
            }
            commonType = classMap.lub(commonType, type);
        }
        return commonType;
    }
    public Symbol visit(CondNode node, MyContext ctx) {
        //e1 : Bool (if)
        Symbol condType = visit(node.getCond(), ctx);
        if(!condType.equals(TreeConstants.Bool)) {
            Error.semant("CondNode: Condition is not a boolean: %s", condType);
        }
        //e2: T2 (then)
        Symbol T2 = visit(node.getElseExpr(), ctx);
        //e3: T3 (else)
        Symbol T3 = visit(node.getThenExpr(), ctx);

        //if e1 then e2 else e3 fi :
        node.setType(join(ctx, T2, T3));
        return node.getType();
    }
    
    // [Sequence]
    @Override
    public Symbol visit(BlockNode node, MyContext ctx) {
        Symbol lastExprType = null;
        ObjectMap nestedO = ctx.objectMap.clone();
        MyContext nestedCtx = ctx.with(nestedO);
        for (ExpressionNode expr : node.getExprs()) {
            visit(expr, nestedCtx);
            lastExprType = expr.getType();
        }
        if (lastExprType.equals(TreeConstants.SELF_TYPE)) {
            node.setType(TreeConstants.SELF_TYPE);
            return ctx.currentClass;
        }
        node.setType(lastExprType);
        return lastExprType;
    }

    // [Let-Init] / [Let-No-Init]
    @Override
    public Symbol visit(LetNode node, MyContext ctx) {
        if(node.getIdentifier().getName().equals("self"))
        {
            Error.semant("LetNode: self in wrong place");
        }

        ClassInfo currentClassInfo = classMap.get(ctx.currentClass);
        Symbol declaredType = node.getType_decl();
        ExpressionNode init = node.getInit();

        Symbol T0Type;
        if (declaredType.equals(TreeConstants.SELF_TYPE)) {
            T0Type = TreeConstants.SELF_TYPE;
        } else {
            T0Type = declaredType;
        }

        if (init instanceof NoExpressionNode) {
            // [Let-No-Init]
            ObjectMap newO = currentClassInfo.objectMap.extend(node.getIdentifier(), T0Type);
            MyContext newCtx = ctx.with(newO);

            visit(node.getBody(), newCtx);
            Symbol T1Type = node.getBody().getType();
            node.setType(T1Type);
            return T1Type;
        }
        else{
            // [Let-Init]
            visit(node.getInit(), ctx);
            Symbol T1Type = node.getInit().getType();
            
            //if T0 is SELF_TYPE, T1 needs to be SELF_TYPE
            if (T0Type.equals(TreeConstants.SELF_TYPE)) {
                if (!T1Type.equals(TreeConstants.SELF_TYPE))
                {
                    Error.semant("LetNode: Unknown error");
                }
            }
            else{
                if(!classMap.inheritsFrom(T1Type, T0Type)){
                    Error.semant("LetNode: Unknown error");
                }
            }
            ObjectMap newO = currentClassInfo.objectMap.extend(node.getIdentifier(), T0Type);
            MyContext newCtx = ctx.with(newO);
    
            visit(node.getBody(), newCtx);
            Symbol T2Type = node.getBody().getType();
            node.setType(T2Type);
            return T2Type;
        }
    }

    // [Case]
    @Override
    public Symbol visit(CaseNode node, MyContext ctx){
        // System.out.println("test1");
        ClassInfo currentClassInfo = classMap.get(ctx.currentClass);
        //e0:T0
        visit(node.getExpr(), ctx);
        // System.out.println("test2");
        //T1..Tn
        List<BranchNode> cases = node.getCases();
        List<Symbol> seenDeclaredTypes = new ArrayList<>();
        List<Symbol> seenBodyTypes = new ArrayList<>();
        //each branch has a name, type_decl, expr
        // System.out.println("test3");
        for (BranchNode branchNode : cases) {
            // System.out.println(String.format("CaseNode: %s, %s", branchNode.getName(), branchNode.getType_decl()));
            ObjectMap newO = currentClassInfo.objectMap.extend(branchNode.getName(), branchNode.getType_decl());
            MyContext newCtx = ctx.with(newO);
    
            visit(branchNode.getExpr(), newCtx);
            //if duplicate of same type, send error
            for (Symbol declaredType : seenDeclaredTypes) {
                if (branchNode.getType_decl().equals(declaredType)){
                    Error.semant("Dupilicant branch type in case statement");
                }
            }
            seenDeclaredTypes.add(branchNode.getType_decl());
            seenBodyTypes.add(branchNode.getExpr().getType());
        }
        // System.out.println("test4");
        //type is the join of all the cases
        Symbol commonParentType = join(ctx, seenBodyTypes.toArray(new Symbol[0]));
        node.setType(commonParentType);
        // System.out.println("test5");
        return commonParentType;
    }

    // [Loop]
    @Override
    public Symbol visit(LoopNode node, MyContext ctx) {
        //if e1 is bool
        Symbol condType = visit(node.getCond(), ctx);
        if(!condType.equals(TreeConstants.Bool)){
            Error.semant("LoopNode: Condition is not a boolean: %s", condType);
        }

        // e2
        visit(node.getBody(), ctx);

        node.setType(TreeConstants.Object_);
        return node.getType();
    }

    // [Isvoid]
    @Override
    public Symbol visit(IsVoidNode node, MyContext ctx) {
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
            Error.semant("CompNode: Invalid type for complement: %s", nodeType.getName());
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
                Error.semant("BoolBinopNode: Expression 1 is not an integer");
            }
            if(!visit(node.getE2(), ctx).equals(TreeConstants.Int)){
                Error.semant("BoolBinopNode: Expression 2 is not an integer");
            }
            node.setType(TreeConstants.Bool);
            return node.getType();
        }
    }

    // [Equal]
    public Boolean isStrictComparableType(Symbol type) {
        return (
            type.equals(TreeConstants.Int) ||
            type.equals(TreeConstants.Str) ||
            type.equals(TreeConstants.Bool)
        );
    }
    @Override
    public Symbol visit(EqNode node, MyContext ctx) {
        Symbol T1 = visit(node.getE1(), ctx);
        Symbol T2 = visit(node.getE2(), ctx);

        // According to the manual:
        // The wrinkle in the rule for equality is that any types may be freely
        // compared except Int, String and Bool, which may only be compared with
        // objects of the same type.
        if ((isStrictComparableType(T1) || isStrictComparableType(T2)) && !T1.equals(T2)) {
            Error.semant("EqNode: Cannot compare values of different types: %s != %s", T1, T2);
        }
        node.setType(TreeConstants.Bool);
        return node.getType();
    }

    // [Neg]
    @Override
    public Symbol visit(NegNode node, MyContext ctx) {
        Symbol nodeType = visit(node.getE1(), ctx);
        if (!nodeType.equals(TreeConstants.Int)) {
            // error
            Error.semant("NegNode: Invalid type for negation: %s", nodeType.getName());
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
            Error.semant("IntBinopNode: error here");
        }
        // O, M, C |- e2 : Int
        Symbol nodeType2 = visit(node.getE2(), ctx);
        if (!nodeType2.equals(TreeConstants.Int)) {
            Error.semant("IntBinopNode: error here");
        }
        // op ∈ {∗, +, −, /}
        // operation is allowed and creates an int
        node.setType(TreeConstants.Int);
        return TreeConstants.Int;
    }

    // [Attr-Init] / [Attr-No-Init]
    @Override
    public Symbol visit(AttributeNode node, MyContext ctx) {
        if(node.getName().getName().equals("self")){
            Error.semant("self in wrong place");
        }

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
                Error.semant("AttributeNode: Initialisation expression '%s' does not have type: '%s'", initType, declaredType);
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
        List<Symbol> formalNames = new ArrayList<Symbol>();
        for (FormalNode formalNode : node.getFormals()) {
            if(formalNode.getName().getName().equals("self")){
                Error.semant("self in wrong place");
                return TreeConstants.No_type;
            }
            if (formalNames.contains(formalNode.getName())){
                Error.semant("duplicate formal parameters");
            }
            formalNames.add(formalNode.getName());
            newObjectMap.put(formalNode.getName(), formalNode.getType_decl());
        }

        // visit the expression using the new object map
        ExpressionNode expr = node.getExpr();
        Symbol exprType = visit(expr, ctx.with(newObjectMap));
        // check that T0' <= T0
        Symbol declaredType = node.getReturn_type();
        if (declaredType.equals(TreeConstants.SELF_TYPE)) {
            if (!(exprType.equals(TreeConstants.self) || expr.getType().equals(TreeConstants.SELF_TYPE))
            ){
                Error.semant("MethodNode: return type is not SELF_TYPE");
            }
            declaredType = ctx.currentClass;
        }
        else if (!classMap.inheritsFrom(exprType, declaredType)) {
            Error.semant(
                "MethodNode: Method expression has incompatible type with declaration");
        }

        return declaredType;
    }
}
