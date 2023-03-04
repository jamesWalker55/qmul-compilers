# example usage:
# .\run.ps1 assignments\pa1\good.cl

$ANTLR_PATH = "."
$BUILD_DIR = "$ANTLR_PATH\assignments\pa1"
$ANTLR_JAR = "$ANTLR_PATH\lib\antlr-4.6-complete.jar"
$CLASSPATH = "$BUILD_DIR;$ANTLR_JAR;"

java -classpath $CLASSPATH Frontend $args
