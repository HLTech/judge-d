#!/bin/bash

MEMORY_OPTIONS=" -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=2 -XshowSettings:vm "
JAVA_OPTIONS=$MEMORY_OPTIONS

if [[ -v VALIDATION_OPTIONS ]]; then
    echo "Selected validation options: $VALIDATION_OPTIONS"
    validation_options_array=(${VALIDATION_OPTIONS//,/ })
    validation_options_array_length=${#validation_options_array[@]}
    for (( i=0; i<${validation_options_array_length}; i++ ));
    do
        validation_options_array[$i]="-Dswagger."${validation_options_array[$i]}
        JAVA_OPTIONS+="${validation_options_array[$i]} "
    done
fi

set -x
java $JAVA_OPTIONS -jar judge-d.jar
