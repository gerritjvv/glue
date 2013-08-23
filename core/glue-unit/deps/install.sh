#!/usr/bin/env bash


abspath=$(cd ${0%/*} && echo $PWD/${0##*/})
BIN_HOME=`dirname $abspath`

for f in scala-compiler scala-library scala-reflect scala-xml scala-parser-combinators; do

echo $f
mvn install:install-file -Dfile=$BIN_HOME/${f}-2.11.0-g.jar -DgroupId=org.scala-lang -DartifactId=$f -Dversion=2.11.0-g -Dpackaging=jar -DpomFile=$BIN_HOME/${f}-2.11.0-g.pom

done


mvn install:install-file -Dfile=$BIN_HOME/groovypp-0.9.0_1.8.2.jar -DgroupId=org.mbte.groovypp -DartifactId=groovypp -Dversion=0.9.0_1.8.2 -Dpackaging=jar -DpomFile=$BIN_HOME/groovypp-0.9.0_1.8.2.pom


