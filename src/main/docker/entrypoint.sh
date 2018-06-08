#!/bin/sh

pwd
ls -al

java \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-Dspring.profiles.active=sit \
$* -jar judge-dredd.jar
