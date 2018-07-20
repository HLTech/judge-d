package dev.hltech.dredd.domain.validation;

import dev.hltech.dredd.domain.contracts.ServiceContracts;

public interface CapabilitiesValidator {

    CapabilitiesValidationResult validate(ServiceContracts consumer);

}
