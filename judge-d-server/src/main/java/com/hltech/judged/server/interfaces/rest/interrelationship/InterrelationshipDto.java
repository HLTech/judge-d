package com.hltech.judged.server.interfaces.rest.interrelationship;

import com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto;
import lombok.Data;

import java.util.Set;

@Data
class InterrelationshipDto {

    private final String environment;
    private final Set<ServiceContractsDto> serviceContracts;
}
