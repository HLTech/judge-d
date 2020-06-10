package com.hltech.judged.agent.config;

import com.hltech.judged.agent.JudgeDPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CachingDelegatingDPublisher {

    private final JudgeDPublisher judgeDPublisher;

    private Set<JudgeDPublisher.ServiceForm> previouslySentEnvironment;

    public CachingDelegatingDPublisher(JudgeDPublisher judgeDPublisher) {
        this.judgeDPublisher = judgeDPublisher;
    }

    public void publish(String environment, String space, Set<JudgeDPublisher.ServiceForm> serviceForms) {
        if (previouslySentEnvironment == null || !previouslySentEnvironment.equals(serviceForms)) {
            log.info("publishing services to Judge-D: " + serviceForms);

            judgeDPublisher.publish(environment, space != null ? space : "default", serviceForms);

            previouslySentEnvironment = new HashSet<>(serviceForms);
        } else {
            log.debug("Services not changed since last update. Skipping update.");
        }
    }
}
