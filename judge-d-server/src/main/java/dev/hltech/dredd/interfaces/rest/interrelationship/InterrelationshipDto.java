package dev.hltech.dredd.interfaces.rest.interrelationship;

import dev.hltech.dredd.interfaces.rest.contracts.ServiceContractsDto;
import lombok.Data;

import java.util.Set;

@Data
public class InterrelationshipDto {

    private final String environment;
    private final Set<ServiceContractsDto> serviceContracts;
}
