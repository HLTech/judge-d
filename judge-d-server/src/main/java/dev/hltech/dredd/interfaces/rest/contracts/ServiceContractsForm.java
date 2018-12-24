package dev.hltech.dredd.interfaces.rest.contracts;

import lombok.Data;

import java.util.Map;

@Data
public class ServiceContractsForm {

    private final Map<String, String> capabilities;
    private final Map<String, Map<String, String>> expectations;
}
