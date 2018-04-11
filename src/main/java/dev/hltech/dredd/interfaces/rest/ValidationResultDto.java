package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidationResultDto {

    List<ConsumerProviderValidationResultDto> verificationResult;

}
