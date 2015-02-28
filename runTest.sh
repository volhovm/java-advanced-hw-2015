#!/bin/sh

init="java"

args="info.kgeorgiy.java.advanced."

case "$1" in
    Walk)
        args=$args"walk.Tester RecursiveWalk ru.ifmo.ctddev.volhov.walk.RecursiveWalk"
        ;;
    ArraySet)
        args=$args"arrayset.Tester NavigableSet ru.ifmo.ctddev.volhov.arrayset.ArraySet"
        ;;
    *) echo "Usage: sh runTest.sh [Walk|ArraySet] [salt]"
       exit
       ;;
esac

cd out/production/java-advanced-hw-2015

jars=$(find ../../../java-advanced-2015/lib -name "*.jar")
classpath=""
for i in $jars
do
    classpath=$classpath$i":"
done

classpath="-cp ../../../java-advanced-2015/artifacts/"$1"Test.jar:"$classpath":."


init=$init" "$classpath" "$args" "$2" "$3" "$4

echo $init
$init