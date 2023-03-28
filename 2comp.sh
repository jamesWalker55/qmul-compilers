if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    $COOL_HOME/2run.sh "$1" > delta_run.txt 2>&1
    $COOL_HOME/2ref.sh "$1" > delta_ref.txt 2>&1
    delta ./delta_ref.txt ./delta_run.txt
fi
