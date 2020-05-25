package com.hltech.judged.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UpdateServicesTask {

    private String environment;
    private String space;
    private ServiceLocator serviceLocator;
    private JudgeDPublisher publisher;

    @Autowired
    public UpdateServicesTask(
        @Value("${hltech.contracts.judge-d.environment}") String environment,
        @Value("${hltech.contracts.judge-d.space:default}") String space,
        ServiceLocator serviceLocator,
        JudgeDPublisher publisher
    ) {
        this.environment = environment;
        this.space = space;
        this.serviceLocator = serviceLocator;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 5_000, initialDelay = 10_000)
    public void updateServices() {
        log.debug("Searching for available services...");
        Set<ServiceLocator.Service> serviceForms = serviceLocator.locateServices();
        log.debug("Done - found following services: " + serviceForms);
        publisher.publish(
            environment,
            space,
            serviceForms.stream().map(UpdateServicesTask::toForm).collect(Collectors.toSet())
        );
    }

    private static JudgeDPublisher.ServiceForm toForm(ServiceLocator.Service service) {
        return new JudgeDPublisher.ServiceForm(service.getName(), service.getVersion());
    }


}
