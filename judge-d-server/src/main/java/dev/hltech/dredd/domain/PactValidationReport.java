package dev.hltech.dredd.domain;

import dev.hltech.dredd.domain.validation.InteractionValidationReport;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PactValidationReport {

    private String consumerName;
    private String providerName;
    private String providerVersion;

    private List<InteractionValidationReport> interactionValidationReports;

}
