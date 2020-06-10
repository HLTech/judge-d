package com.hltech.judged.server.interfaces.rest.contracts;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ServiceContractsForm {

    private final Map<String, ContractForm> capabilities;
    private final Map<String, Map<String, ContractForm>> expectations;

    @Data
    public static class ContractForm implements Serializable {
        private final String value;
        private final String mimeType;
    }
}
