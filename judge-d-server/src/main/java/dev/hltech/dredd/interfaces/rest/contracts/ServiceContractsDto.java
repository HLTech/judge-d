package dev.hltech.dredd.interfaces.rest.contracts;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ServiceContractsDto {

    private final String name;
    private final String version;

    private final Map<String, ContractDto> capabilities;
    private final Map<String, Map<String, ContractDto>> expectations;

    @Data
    public static class ContractDto implements Serializable {
        private final String value;
        private final String mimeType;
    }
}
