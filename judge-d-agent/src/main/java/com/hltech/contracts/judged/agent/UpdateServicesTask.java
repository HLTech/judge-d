package com.hltech.contracts.judged.agent;

import com.hltech.contracts.judged.agent.JudgeDPublisher.ServiceForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UpdateServicesTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServicesTask.class);

    private String environment;
    private ServiceLocator serviceLocator;
    private JudgeDPublisher publisher;

    @Autowired
    public UpdateServicesTask(@Value("${hltech.contracts.judge-d.environment}") String environment, ServiceLocator serviceLocator, JudgeDPublisher publisher) {
        this.environment = environment;
        this.serviceLocator = serviceLocator;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 5_000, initialDelay = 10_000)
    public void updateServices() {
        LOGGER.debug("Searching for available services...");
        Set<ServiceLocator.Service> serviceForms = serviceLocator.locateServices();
        LOGGER.debug("Done - found following services: " + serviceForms);
        publisher.publish(
            environment,
            serviceForms.stream().map(UpdateServicesTask::toForm).collect(Collectors.toSet())
        );
    }

    private static ServiceForm toForm(ServiceLocator.Service service) {
        return new ServiceForm(service.getName(), service.getVersion());
    }


}
