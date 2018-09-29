#!/bin/sh
java \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-XX:MaxRAMFraction=2 \
-XshowSettings:vm \
$* -jar judge-d-agent.jar
