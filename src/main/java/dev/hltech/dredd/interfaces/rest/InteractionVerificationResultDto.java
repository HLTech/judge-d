package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InteractionVerificationResultDto {

    private String name;
    private String verificationResult;
    private List<String> errors;

}
