# create folder for test output
mkdir -p $COOL_HOME/testoutput

# output path
DATE=$(date '+%Y-%m-%d_%H-%M-%S')
OUTPUT_PATH="$COOL_HOME/testoutput/$DATE.txt"

cd $COOL_HOME/assignments/pa2
date '+%Y-%m-%d %H:%M:%S'
testme semant 2>&1 | tee $OUTPUT_PATH
