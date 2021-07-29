package com.hltech.judged.server.interfaces.rest.environment;

import lombok.Value;

import java.util.Set;

@Value
public class EnvironmentDto {
    String name;
    Set<SpaceDto> spaces;

    @Value
    public static class SpaceDto {
        String name;
        Set<ServiceDto> services;

        @Value
        public static class ServiceDto {
            String name;
            String version;
        }
    }
}
