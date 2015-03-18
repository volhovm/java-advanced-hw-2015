#!/bin/bash

dir="ImplTemp"
rm -vrf $dir
mkdir -v $dir
echo "Compiling sources..."
javac -d ImplTemp -cp lib/ImplementorTest.jar src/ru/ifmo/ctddev/volhov/implementor/*.java
echo "Creating jar..."
cd $dir
#mv -v $(find . -iname "*.class") .
cp -v ../lib/ImplementorTest.jar .
touch Manifest
echo "Manifest-Version: 1.0
Main-Class: ru.ifmo.ctddev.volhov.implementor.Implementor
Class-Path: "$(find .. -iname "ImplementorTest.jar") >> Manifest
targetFiles=$(find . -iname "*.class"
              # -or -iname "*.jar"
           )
echo "Including in jar: "$targetFiles
echo "Placing jar to artifacts/MyImplementor.jar"
mkdir ../artifacts
jar -cfm ../artifacts/MyImplementor.jar Manifest $targetFiles
cd ..
rm -rf $dir
