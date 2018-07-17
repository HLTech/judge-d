package dev.hltech.dredd.interfaces.rest.contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceContractsForm {

    private Map<String, String> capabilities;
    private Map<String, Map<String, String>> expectations;

}
