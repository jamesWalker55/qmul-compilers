# To run refsemant, do this:
# ./assignments/pa2/refsemant testsuite/semant/dispatch.test

if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    # determine actual path of input file
    input_script=$(readlink -m "$1")
    # # build the frontend first
    # echo "Building frontend..."
    # cd $COOL_HOME/assignments/pa1
    # buildme frontend
    # then build the semant
    echo "Building semant..."
    cd $COOL_HOME/assignments/pa2
    buildme semant
    # finally run the semant
    echo "Running mysemant..."
    # cd $last_working_dir
    ./mysemant "$input_script"
fi
