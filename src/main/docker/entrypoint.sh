#!/bin/sh

java \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-Dspring.profiles.active=docker \
$* -jar contract-verifier.jar
