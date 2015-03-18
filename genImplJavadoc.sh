#!/bin/bash

rm -rf implementorJavadoc/*
implFiles=$(find src/ru/ifmo/ctddev/volhov/implementor/ -iname "*.java")
implTestFiles=$(ls java-advanced-2015/java/info/kgeorgiy/java/advanced/implementor/{Impler,JarImpler,ImplerException}.java)
mkdir implementorJavadoc
javadoc -d implementorJavadoc -linkoffline http://docs.oracle.com/javase/7/docs/api/ http://docs.oracle.com/javase/7/docs/api/ -private $implFiles $implTestFiles
