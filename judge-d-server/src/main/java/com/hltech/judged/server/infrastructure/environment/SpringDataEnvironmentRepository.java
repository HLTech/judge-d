package com.hltech.judged.server.infrastructure.environment;

import com.hltech.judged.server.domain.environment.EnvironmentAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

interface SpringDataEnvironmentRepository extends JpaRepository<EnvironmentAggregate, String> {

    @Query("SELECT e.name FROM EnvironmentAggregate e")
    Set<String> getNames();

}
