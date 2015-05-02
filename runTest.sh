#!/bin/sh

init="java"

args="info.kgeorgiy.java.advanced."
package_name=$1"Test.jar"
case "$1" in
    Walk)
        args=$args"walk.Tester RecursiveWalk ru.ifmo.ctddev.volhov.walk.RecursiveWalk"
        ;;
    ArraySet)
        args=$args"arrayset.Tester NavigableSet ru.ifmo.ctddev.volhov.arrayset.ArraySet"
        ;;
    Implementor)
        args=$args"implementor.Tester class ru.ifmo.ctddev.volhov.implementor.Implementor"
        ;;
    IterativeParallelism)
        args=$args"concurrent.Tester list ru.ifmo.ctddev.volhov.iterativeparallelism.IterativeParallelism"
        ;;
    ParallelMapper)
        args=$args"mapper.Tester list ru.ifmo.ctddev.volhov.iterativeparallelism.ParallelMapperImpl,ru.ifmo.ctddev.volhov.iterativeparallelism.IterativeParallelism"
        ;;
    WebCrawler)
        args=$args"crawler.Tester hard ru.ifmo.ctddev.volhov.crawler.WebCrawler"
        ;;
    HelloUDPClient)
    	args=$args"hello.Tester client ru.ifmo.ctddev.volhov.hello.HelloUDPClient"
	package_name="HelloUDPTest.jar"
	;;
    HelloUDPServer)
    	args=$args"hello.Tester server ru.ifmo.ctddev.volhov.hello.HelloUDPServer"
	package_name="HelloUDPTest.jar"
	;;
    *) echo "Usage: sh runTest.sh [Walk|ArraySet|Implementor|IterativeParallelism|ParallelMapper|WebCrawler|HelloUDP[Client|Server]] [salt]"
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

classpath="-cp ../../../java-advanced-2015/artifacts/"$package_name":"$classpath":."


init=$init" "$classpath" "$args" "$2" "$3" "$4

echo $init
$init
