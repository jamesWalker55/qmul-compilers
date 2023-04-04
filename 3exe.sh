# run a single .s file

if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    coolspim -file "$1"
fi
