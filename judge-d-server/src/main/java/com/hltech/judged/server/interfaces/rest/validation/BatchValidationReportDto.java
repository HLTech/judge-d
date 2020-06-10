package com.hltech.judged.server.interfaces.rest.validation;

import com.hltech.judged.server.interfaces.rest.environment.ServiceDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchValidationReportDto {

    private ServiceDto service;
    private List<ContractValidationReportDto> validationReports;

}
