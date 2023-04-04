# create folder for test output
mkdir -p $COOL_HOME/testoutput3

# output path
DATE=$(date '+%Y-%m-%d_%H-%M-%S')
OUTPUT_PATH="$COOL_HOME/testoutput3/$DATE.txt"

cd $COOL_HOME/assignments/pa3
date '+%Y-%m-%d %H:%M:%S'
testme cgen 2>&1 | tee $OUTPUT_PATH
