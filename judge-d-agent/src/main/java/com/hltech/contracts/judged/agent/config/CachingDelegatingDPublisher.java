package com.hltech.contracts.judged.agent.config;

import com.google.common.collect.Sets;
import com.hltech.contracts.judged.agent.JudgeDPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class CachingDelegatingDPublisher implements JudgeDPublisher {

    private final JudgeDPublisher judgeDPublisher;

    private Set<ServiceForm> previouslySentEnvironment;

    public CachingDelegatingDPublisher(JudgeDPublisher judgeDPublisher) {
        this.judgeDPublisher = judgeDPublisher;
    }

    @Override
    public void publish(String environment, Set<ServiceForm> serviceForms) {
        if (previouslySentEnvironment == null || !previouslySentEnvironment.equals(serviceForms)) {
            log.info("publishing services to Judge-D: " + serviceForms);

            judgeDPublisher.publish(environment, serviceForms);

            previouslySentEnvironment = Sets.newHashSet(serviceForms);
        } else {
            log.debug("Services not changed since last update. Skipping update.");
        }
    }
}
