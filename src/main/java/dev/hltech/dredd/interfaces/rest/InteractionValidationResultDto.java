package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InteractionValidationResultDto {

    private String name;
    private String verificationResult;
    private List<String> errors;

}
