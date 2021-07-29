package com.hltech.judged.server.interfaces.rest.validation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchValidationReportDto {

    private ServiceDto service;
    private List<ContractValidationReportDto> validationReports;

}
