#!/usr/bin/bash

# TODO: sort out classpath mess
JAR_DIR="etc/jars"
JAVA_CP="out/production/classes"
for JAR in ${JAR_DIR}/*.jar
do
    JAVA_CP="$JAVA_CP:$JAR"
done

groovysh -cp ${JAVA_CP}