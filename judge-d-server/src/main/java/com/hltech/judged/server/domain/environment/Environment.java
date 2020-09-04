package com.hltech.judged.server.domain.environment;

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

    public Set<Service> getServices(String spaceName) {
       return spaces.stream()
           .filter(space -> space.getName().equals(spaceName))
           .findAny()
           .map(Space::getServices)
           .orElse(new HashSet<>());
    }

    public Set<Service> getAllServices() {
        return spaces.stream()
            .flatMap(space -> space.getServices().stream())
            .collect(Collectors.toUnmodifiableSet());
    }
}
