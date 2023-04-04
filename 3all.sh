# run a single .s file

if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    ./3ref.sh "$1"
    ./3fin.sh "$1"
fi
