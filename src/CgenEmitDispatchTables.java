import ast.Symbol;

class CgenEmitDispatchTables extends CgenVisitor {

    // Emit the dispatch table of a class
    @Override
    Void visit(CgenNode v) {
      /* WIP */
      Cgen.emitter.codeDispatchTable(v.env);
      super.visit(v);
        return null;
    }
}
