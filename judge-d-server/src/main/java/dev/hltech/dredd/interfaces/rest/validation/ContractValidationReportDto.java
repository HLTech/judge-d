package dev.hltech.dredd.interfaces.rest.validation;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ContractValidationReportDto {

    private ConsumerAndProviderDto consumerAndProvider;

    private List<InteractionValidationReportDto> interactions = Lists.newLinkedList();

    public ContractValidationReportDto(ConsumerAndProviderDto consumerAndProvider) {
        this.consumerAndProvider = consumerAndProvider;
    }

    public void addInteractions(List<InteractionValidationReportDto> interactions) {
        this.interactions.addAll(interactions);
    }

}
