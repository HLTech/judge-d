package dev.hltech.dredd.interfaces.rest;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AggregatedValidationResultDto {

    List<ContractValidationResultDto> validationResults;

}
