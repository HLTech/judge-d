package com.hltech.judged.server.domain.environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class JPAEnvironmentRepository implements EnvironmentRepository {

    private SpringDataEnvironmentRepository springDataEnvironmentRepository;

    @Autowired
    public JPAEnvironmentRepository(SpringDataEnvironmentRepository springDataEnvironmentRepository) {
        this.springDataEnvironmentRepository = springDataEnvironmentRepository;
    }

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
