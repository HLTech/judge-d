package dev.hltech.dredd.interfaces.rest.contracts;

import lombok.Data;

import java.util.Map;

@Data
public class ServiceContractsDto {

    private final String name;
    private final String version;

    private final Map<String, String> capabilities;
    private final Map<String, Map<String, String>> expectations;

}
