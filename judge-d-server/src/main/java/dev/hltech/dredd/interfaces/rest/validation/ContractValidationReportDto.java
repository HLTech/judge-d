package dev.hltech.dredd.interfaces.rest.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ContractValidationReportDto {

    private String consumerName;
    private String consumerVersion;
    private String providerName;
    private String providerVersion;

    private ContractValidationStatus validationStatus;

    private List<InteractionValidationReportDto> interactions;

}
