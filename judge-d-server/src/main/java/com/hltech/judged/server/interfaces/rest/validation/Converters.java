package com.hltech.judged.server.interfaces.rest.validation;

import com.google.common.collect.Ordering;
import com.hltech.judged.server.domain.ServiceId;
import com.hltech.judged.server.domain.validation.EnvironmentValidatorResult;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;

@NoArgsConstructor
public class Converters {

    public static List<ContractValidationReportDto> toDtos(ServiceId serviceId, Collection<EnvironmentValidatorResult> validationResults) {
        Map<ConsumerAndProviderDto, ContractValidationReportDto> result = newHashMap();
        validationResults
            .forEach(environmentValidatorResult -> {
                environmentValidatorResult.getCapabilitiesValidationResults()
                    .forEach(cvr -> {
                        ConsumerAndProviderDto key = new ConsumerAndProviderDto(cvr.getConsumerName(), cvr.getConsumerVersion(), serviceId.getName(), serviceId.getVersion());
                        if (!result.containsKey(key)) {
                            result.put(
                                key,
                                new ContractValidationReportDto(key)
                            );
                        }
                        result.get(key).addInteractions(
                            cvr.getInteractionValidationResults()
                                .stream()
                                .map(ivr -> new InteractionValidationReportDto(
                                    environmentValidatorResult.getCommunicationInterface(),
                                    ivr.getName(),
                                    ivr.getStatus(),
                                    ivr.getErrors()
                                ))
                                .collect(Collectors.toList())
                        );
                    });
                environmentValidatorResult.getExpectationValidationResults()
                    .forEach(evr -> {
                        ConsumerAndProviderDto key = new ConsumerAndProviderDto(serviceId.getName(), serviceId.getVersion(), evr.getProviderName(), evr.getProviderVersion());
                        if (!result.containsKey(key)) {
                            result.put(
                                key,
                                new ContractValidationReportDto(key)
                            );
                        }
                        result.get(key).addInteractions(
                            evr.getInteractionValidationResults()
                                .stream()
                                .map(ivr -> new InteractionValidationReportDto(
                                    environmentValidatorResult.getCommunicationInterface(),
                                    ivr.getName(),
                                    ivr.getStatus(),
                                    ivr.getErrors()
                                ))
                                .collect(Collectors.toList())
                        );
                    });
            });

        return Ordering
            .from((Comparator<ContractValidationReportDto>) (o1, o2) -> o1.getConsumerAndProvider().toString().compareTo(o2.getConsumerAndProvider().toString()))
            .sortedCopy(result.values());
    }
}
