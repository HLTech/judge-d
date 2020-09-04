package com.hltech.judged.server.infrastructure.persistence.environment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

interface SpringDataEnvironmentRepository extends JpaRepository<EnvironmentTuple, String> {

    @Query("SELECT e.name FROM EnvironmentTuple e")
    Set<String> getNames();

}
