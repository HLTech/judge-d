package com.hltech.judged.agent.consul;

import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.ecwid.consul.v1.health.HealthClient;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.hltech.judged.agent.ServiceLocator;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ConsulTagBasedServiceLocator implements ServiceLocator {

    private static final String VERSION_TAG_PREFIX = "version=";
    private static final String TAG_SEPARATOR = "=";

    private final CatalogClient catalogConsulClient;
    private final HealthClient healthConsulClient;

    @Override
    public Set<ServiceLocator.Service> locateServices() {
        return catalogConsulClient.getCatalogServices(CatalogServicesRequest.newBuilder().build())
            .getValue().entrySet().stream()
            .filter(services -> hasVersionTag(services.getValue()))
            .filter(it -> isHealthy(it.getKey()))
            .map(services -> toJudgeService(services.getKey(), services.getValue()))
            .collect(Collectors.toSet());
    }

    private ServiceLocator.Service toJudgeService(String serviceName, List<String> tags) {
        return new ServiceLocator.Service(
            serviceName,
            extractVersionFromTags(tags)
        );
    }

    private String extractVersionFromTags(List<String> tags) {
        return tags.stream()
            .filter(this::isVersionTag)
            .map(this::extractVersionFromTag)
            .findFirst()
            .orElse(null);
    }

    private String extractVersionFromTag(String versionTag) {
        return versionTag.split(TAG_SEPARATOR)[1];
    }

    private boolean hasVersionTag(List<String> tags) {
        return tags.stream().anyMatch(this::isVersionTag);
    }

    private boolean isVersionTag(String tag) {
        return tag.contains(VERSION_TAG_PREFIX);
    }

    private boolean isHealthy(String serviceName) {
        HealthServicesRequest request = HealthServicesRequest.newBuilder().setPassing(true).build();
        return healthConsulClient.getHealthServices(serviceName, request).getValue().size() > 0;
    }
}
