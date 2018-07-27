package dev.hltech.dredd.domain.environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static java.util.Optional.ofNullable;

@Repository
public class JPAEnvironmentRepository implements EnvironmentRepository {

    private SpringDataEnvironmentRepository springDataEnvironmentRepository;

    @Autowired
    public JPAEnvironmentRepository(SpringDataEnvironmentRepository springDataEnvironmentRepository) {
        this.springDataEnvironmentRepository = springDataEnvironmentRepository;
    }

    @Override
    public EnvironmentAggregate persist(EnvironmentAggregate environment) {
        return springDataEnvironmentRepository.save(environment);
    }

    @Override
    public Set<String> getNames() {
        return springDataEnvironmentRepository.getNames();
    }

    @Override
    public EnvironmentAggregate get(String name) {
        return ofNullable(springDataEnvironmentRepository.findOne(name)).orElse(EnvironmentAggregate.empty(name));
    }
}