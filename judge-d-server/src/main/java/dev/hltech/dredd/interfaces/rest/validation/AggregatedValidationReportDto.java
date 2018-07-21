package dev.hltech.dredd.interfaces.rest.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AggregatedValidationReportDto {

    List<ContractValidationReportDto> validationResults;

}
