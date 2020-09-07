package com.hltech.judged.server.domain.environment;

import com.hltech.judged.server.domain.ServiceId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@RequiredArgsConstructor
public class Environment {

    public static final String DEFAULT_NAMESPACE = "default";

    private final String name;
    private final Set<Space> spaces;

    public Set<String> getSpaceNames() {
        return spaces.stream()
            .map(Space::getName)
            .collect(Collectors.toUnmodifiableSet());
    }

    public Set<ServiceId> getServices(String spaceName) {
       return spaces.stream()
           .filter(space -> space.getName().equals(spaceName))
           .findAny()
           .map(Space::getServiceIds)
           .orElse(new HashSet<>());
    }

    public Set<ServiceId> getAllServices() {
        return spaces.stream()
            .flatMap(space -> space.getServiceIds().stream())
            .collect(Collectors.toUnmodifiableSet());
    }
}
