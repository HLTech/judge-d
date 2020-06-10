package com.hltech.judged.agent.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.hltech.judged.agent.ServiceLocator;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class ConsulTagBasedServiceLocator implements ServiceLocator {

    private static final String VERSION_TAG_PREFIX = "version=";
    private static final String TAG_SEPARATOR = "=";

    private final ConsulClient consulAgentClient;

    @Override
    public Set<ServiceLocator.Service> locateServices() {
        return consulAgentClient.getAgentServices().getValue().values().stream()
            .filter(service -> hasVersionTag(service.getTags()))
            .map(this::toJudgeService)
            .collect(Collectors.toSet());
    }

    private ServiceLocator.Service toJudgeService(com.ecwid.consul.v1.agent.model.Service service) {
        return new ServiceLocator.Service(
            service.getService(),
            extractVersionFromTags(service.getTags())
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
}
