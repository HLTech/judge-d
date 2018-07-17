package dev.hltech.dredd.interfaces.rest.contracts;

import lombok.Data;

import java.util.Map;

@Data
public class ServiceContractsDto {

    private String name;
    private String version;

    private Map<String, String> capabilities;
    private Map<String, Map<String, String>> expectations;

}
