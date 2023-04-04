# compile using your backend, then run it

if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    # get absolute path of input file
    input_script=$(readlink -m "$1")
    output_script=$(readlink -m "qfin.s")

    # build the semant
    echo "Building backend..."
    cd $COOL_HOME/assignments/pa3
    buildme backend

    # finally run the semant
    echo "Running mybackend..."
    ./mybackend "$input_script" -o $output_script
    echo "Generated output at: $output_script"

    # and finally run the output script
    coolspim -file "$output_script"
fi
