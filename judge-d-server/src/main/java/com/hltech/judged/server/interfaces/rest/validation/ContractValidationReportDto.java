package com.hltech.judged.server.interfaces.rest.validation;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ContractValidationReportDto {

    private final ConsumerAndProviderDto consumerAndProvider;
    private final List<InteractionValidationReportDto> interactions = Lists.newLinkedList();

    public void addInteractions(List<InteractionValidationReportDto> interactions) {
        this.interactions.addAll(interactions);
    }

}
