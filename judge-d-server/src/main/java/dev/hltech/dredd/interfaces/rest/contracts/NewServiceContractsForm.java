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
public class NewServiceContractsForm {

    private Map<String, ContractForm> capabilities;
    private Map<String, Map<String, ContractForm>> expectations;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class ContractForm implements Serializable {
        private String value;
        private String mimeType;
    }
}
