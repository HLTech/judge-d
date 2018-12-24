package dev.hltech.dredd.interfaces.rest.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewServiceContractsDto {

    private String name;
    private String version;

    private Map<String, ContractDto> capabilities;
    private Map<String, Map<String, ContractDto>> expectations;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class ContractDto implements Serializable {
        private String value;
        private String mimeType;
    }
}
