package com.hltech.judged.server.infrastructure.persistence.environment;

import com.hltech.judged.server.domain.environment.Environment;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JPAEnvironmentRepository implements EnvironmentRepository {

    private final SpringDataEnvironmentRepository springDataEnvironmentRepository;

    @Override
    public Environment persist(Environment environment) {
        return springDataEnvironmentRepository.saveAndFlush(environment);
    }

    @Override
    public Set<String> getNames() {
        return springDataEnvironmentRepository.getNames();
    }

    @Override
    public Environment get(String name) {
        return springDataEnvironmentRepository.findById(name).orElse(Environment.empty(name));
    }
}
