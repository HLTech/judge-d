package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ContractValidationReportDto {

    private String consumerName;
    private String consumerVersion;
    private String providerName;
    private String providerVersion;

    private ContractValidationStatus validationStatus;

    private List<InteractionValidationReportDto> interactions;

}
