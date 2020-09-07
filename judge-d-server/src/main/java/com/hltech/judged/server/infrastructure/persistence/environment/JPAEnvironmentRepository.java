package com.hltech.judged.server.infrastructure.persistence.environment;

import com.hltech.judged.server.domain.environment.Environment;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import com.hltech.judged.server.domain.ServiceId;
import com.hltech.judged.server.domain.environment.Space;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JPAEnvironmentRepository implements EnvironmentRepository {

    private final SpringDataEnvironmentRepository springDataEnvironmentRepository;

    @Override
    public Environment persist(Environment environment) {
        EnvironmentTuple environmentTuple = toEnvironmentTuple(environment);
        return toEnvironment(springDataEnvironmentRepository.saveAndFlush(environmentTuple));
    }

    @Override
    public Set<String> getNames() {
        return springDataEnvironmentRepository.getNames();
    }

    @Override
    public Environment get(String name) {
        return springDataEnvironmentRepository.findById(name)
            .map(this::toEnvironment)
            .orElse(new Environment(name, new HashSet<>()));
    }

    private EnvironmentTuple toEnvironmentTuple(Environment environment) {
        return new EnvironmentTuple(environment.getName(), getSpaceServiceVersions(environment));
    }

    private Set<ServiceVersion> getSpaceServiceVersions(Environment environment) {
        return environment.getSpaceNames().stream()
            .flatMap(space -> environment.getServices(space).stream()
                .map(service -> new ServiceVersion(space, service.getName(), service.getVersion())))
            .collect(Collectors.toUnmodifiableSet());
    }

    private Environment toEnvironment(EnvironmentTuple environmentTuple) {
        Set<Space> spaces = new HashSet<>();

        environmentTuple.getServiceVersions()
            .forEach(serviceVersion -> {
                Optional<Space> foundSpace = spaces.stream()
                    .filter(space -> space.getName().equals(serviceVersion.getSpace()))
                    .findAny();

                if (foundSpace.isPresent()) {
                    Set<ServiceId> serviceIds = new HashSet<>(foundSpace.get().getServiceIds());
                    serviceIds.add(new ServiceId(serviceVersion.getName(), serviceVersion.getVersion()));

                    spaces.add(new Space(foundSpace.get().getName(), serviceIds));
                    return;
                }

                spaces.add(new Space(serviceVersion.getSpace(), Set.of(new ServiceId(serviceVersion.getName(), serviceVersion.getVersion()))));
            });

        return new Environment(environmentTuple.getName(), spaces);
    }

}
