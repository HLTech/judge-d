package com.hltech.judged.server.infrastructure.environment;

import com.hltech.judged.server.domain.environment.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

interface SpringDataEnvironmentRepository extends JpaRepository<Environment, String> {

    @Query("SELECT e.name FROM Environment e")
    Set<String> getNames();

}
