package dev.hltech.dredd.domain;

import dev.hltech.dredd.domain.environment.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PactValidationReport {

    private String consumerName;
    private Provider provider;

    private List<InteractionValidationReport> interactionValidationReports;

}
