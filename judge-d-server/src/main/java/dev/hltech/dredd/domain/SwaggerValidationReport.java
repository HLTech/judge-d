package dev.hltech.dredd.domain;

import dev.hltech.dredd.domain.validation.InteractionValidationReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SwaggerValidationReport {

    private String consumerName;
    private String consumerVersion;
    private String providerName;

    private List<InteractionValidationReport> interactionValidationReports;

}
