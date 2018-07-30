#!/bin/sh
java \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
$* -jar judge-d-agent.jar
