# To run refsemant, do this:
# ./assignments/pa2/refsemant testsuite/semant/dispatch.test

if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    # get absolute path of input file
    input_script=$(readlink -m "$1")

    # build the semant
    echo "Building semant..."
    cd $COOL_HOME/assignments/pa2
    buildme semant

    # finally run the semant
    echo "Running mysemant..."
    ./mysemant "$input_script"
fi
