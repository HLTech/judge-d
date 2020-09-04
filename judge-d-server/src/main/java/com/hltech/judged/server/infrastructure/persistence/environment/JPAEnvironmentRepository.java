package com.hltech.judged.server.infrastructure.persistence.environment;

import com.hltech.judged.server.domain.ServiceVersion;
import com.hltech.judged.server.domain.SpaceServiceVersion;
import com.hltech.judged.server.domain.environment.Environment;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
            .orElse(Environment.empty(name));
    }

    private EnvironmentTuple toEnvironmentTuple(Environment environment) {
        return new EnvironmentTuple(environment.getName(), getSpaceServiceVersions(environment));
    }

    private Set<SpaceServiceVersion> getSpaceServiceVersions(Environment environment) {
        return environment.getSpaceNames().stream()
            .flatMap(space -> environment.getServices(space).stream()
                .map(serviceVersion -> new SpaceServiceVersion(space, serviceVersion.getName(), serviceVersion.getVersion())))
            .collect(Collectors.toUnmodifiableSet());
    }

    private Environment toEnvironment(EnvironmentTuple environmentTuple) {
        Environment.EnvironmentBuilder environmentBuilder = Environment.builder(environmentTuple.getName());

        environmentTuple.getSpaceServiceVersions().forEach(spaceServiceVersion ->
            environmentBuilder.withServiceVersion(
                spaceServiceVersion.getSpace(),
                new ServiceVersion(spaceServiceVersion.getName(), spaceServiceVersion.getVersion())
            )
        );

        return environmentBuilder.build();
    }
}
