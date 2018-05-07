#!/bin/sh

java \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-Dspring.profiles.active=sit \
$* -jar contract-verifier.jar
