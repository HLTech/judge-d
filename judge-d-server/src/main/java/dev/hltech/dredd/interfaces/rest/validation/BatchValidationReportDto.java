package dev.hltech.dredd.interfaces.rest.validation;

import dev.hltech.dredd.interfaces.rest.environment.ServiceDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchValidationReportDto {

    private ServiceDto service;
    private List<ContractValidationReportDto> validationReports;

}
