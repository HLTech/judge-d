package com.hltech.judged.agent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

public interface ServiceLocator {

    Set<Service> locateServices();

    @Getter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    class Service {

        private final String name;
        private final String version;

    }

}
