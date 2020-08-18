package com.hltech.judged.server.infrastructure.environment;

import com.hltech.judged.server.domain.environment.EnvironmentAggregate;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JPAEnvironmentRepository implements EnvironmentRepository {

    private final SpringDataEnvironmentRepository springDataEnvironmentRepository;

    @Override
    public EnvironmentAggregate persist(EnvironmentAggregate environment) {
        return springDataEnvironmentRepository.saveAndFlush(environment);
    }

    @Override
    public Set<String> getNames() {
        return springDataEnvironmentRepository.getNames();
    }

    @Override
    public EnvironmentAggregate get(String name) {
        return springDataEnvironmentRepository.findById(name).orElse(EnvironmentAggregate.empty(name));
    }
}
