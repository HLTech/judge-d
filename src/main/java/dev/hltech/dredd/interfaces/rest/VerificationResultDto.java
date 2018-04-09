package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VerificationResultDto {

    List<ConsumerProviderVerificationResultDto> verificationResult;

}
