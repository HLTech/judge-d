#!/bin/sh

if [[ -z "$VALIDATION_OPTIONS" ]]; then
    echo "Selected validation options: ${VALIDATION_OPTIONS}"
    validation_options_array=`echo ${VALIDATION_OPTIONS} | tr ',' ' '`
    for i in ${validation_options_array}; do
      SWAGGER_OPTIONS+=" -Dswagger."$i
    done
fi

set -x
java ${SWAGGER_OPTIONS} -jar judge-d.jar
