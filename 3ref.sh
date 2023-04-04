# compile using the reference backend, then run it
if [ -z "$1" ]
then
    echo "Please provide a path to a cool file."
else
    coolc "$1" -o qref.s
    coolspim -file "qref.s"
fi
