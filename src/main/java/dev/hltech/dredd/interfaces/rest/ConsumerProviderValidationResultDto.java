package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ConsumerProviderValidationResultDto {

    private String consumerName;
    private String consumerVersion;
    private String providerName;
    private String providerVersion;

    private List<InteractionValidationResultDto> interactions;

}
