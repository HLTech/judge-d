#!/bin/sh
java \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-XX:MaxRAMFraction=1 \
-XshowSettings:vm \
$* -jar judge-d-agent.jar
