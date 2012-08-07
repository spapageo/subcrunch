base=$(dirname $file) 
cd $base
java -jar subcrunch.jar "$1" "$2" "$3" 
