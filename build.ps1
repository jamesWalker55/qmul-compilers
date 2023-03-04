$ANTLR_PATH = "."
$BUILD_DIR = "$ANTLR_PATH\assignments\pa1"
$ANTLR_JAR = "$ANTLR_PATH\lib\antlr-4.6-complete.jar"
$SRC_DIR = "$ANTLR_PATH\src"
$CLASSPATH = "$BUILD_DIR;$ANTLR_JAR;"

$JAVA_SRC_FILE_LIST = @(
  "$SRC_DIR/ast/*.java"
  "$SRC_DIR/ast/visitor/*.java"
  "$SRC_DIR/Flags.java"
  "$SRC_DIR/Utilities.java"
  "$SRC_DIR/StringTable.java"
  "$SRC_DIR/TreeConstants.java"
  "$SRC_DIR/ASTBuilder.java"
  "$SRC_DIR/CoolErrorListener.java"
  "$SRC_DIR/CoolErrorStrategy.java"
  "$SRC_DIR/Frontend.java"
  "$BUILD_DIR/*.java"
)

java -jar $ANTLR_JAR -o $BUILD_DIR -listener -visitor -lib $SRC_DIR $SRC_DIR/CoolParser.g4 $SRC_DIR/CoolLexer.g4

javac -classpath $CLASSPATH -d "$BUILD_DIR/" $JAVA_SRC_FILE_LIST
