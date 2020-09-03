package com.hltech.judged.server.domain


import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository
import com.hltech.judged.server.domain.validation.EnvironmentValidatorResult
import com.hltech.judged.server.domain.validation.ping.PingContractValidator
import org.springframework.http.MediaType
import spock.lang.Specification

import static org.assertj.core.util.Lists.newArrayList

class JudgeDUT extends Specification {

    def serviceContractsRepository = new InMemoryServiceContractsRepository();
    def environmentRepository = new InMemoryEnvironmentRepository()
    def contractValidator = new PingContractValidator()
    def judgeD = new JudgeDApplicationService(environmentRepository, serviceContractsRepository)

    def 'validate expectations against environment without provider'() {
        given:
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('validated-consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment('test-env', [] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                consumer,
                ["test-env"] as List,
                contractValidator
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 0
            evr.getExpectationValidationResults().size() == 1
    }

    def 'validate expectations against environment with provider '() {
        given:
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('validated-consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [new ServiceVersion("provider", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                consumer,
                newArrayList("test-env"),
                contractValidator
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 0
            evr.getExpectationValidationResults().size() == 1

    }

    def 'validate capabilities against environment with consumer'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('provider-x', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('consumer', '1.0'),
                [],
                [new Expectation('provider-x', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [new ServiceVersion("consumer", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                provider,
                newArrayList("test-env"),
                contractValidator
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 1
            evr.getExpectationValidationResults().size() == 0
    }

    def 'validate capabilities against environment without a consumer'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('provider-x', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            environmentRepository.persist(new Environment("test-env", [new ServiceVersion("consumer", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                provider,
                newArrayList("test-env"),
                contractValidator
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 0
            evr.getExpectationValidationResults().size() == 0
    }

    def 'validate capabilities against multiple environments'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('provider-x', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('consumer', '1.0'),
                [],
                [new Expectation('provider-x', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            def consumer2 = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('consumer2', '1.0'),
                [],
                [new Expectation('provider-x', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [new ServiceVersion("consumer", "1.0")] as Set))
            environmentRepository.persist(new Environment("test-env2", [new ServiceVersion("consumer2", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                provider,
                newArrayList("test-env", "test-env2"),
                contractValidator
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 2
            evr.getExpectationValidationResults().size() == 0
    }

    def 'validate contracts of set of services against empty environment'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('provider', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [] as Set))
        when:
            def validationResult = judgeD.validatedServicesAgainstEnvironment(
                [provider, consumer] as List,
                "test-env",
                contractValidator
            )
        then:
            validationResult.containsKey(provider.getId());
            validationResult.containsKey(consumer.getId());

    }

    def 'validate contracts of set of services against env containing all services but with different version'() {
        given:
            def providerOld = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('provider', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumerOld = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('1233456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            def providerNew = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('provider', '2.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumerNew = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceVersion('consumer', '2.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
        environmentRepository.persist(new Environment("test-env", [providerOld.getId(), consumerOld.getId()] as Set))
        when:
            def validationResult = judgeD.validatedServicesAgainstEnvironment(
                [providerNew, consumerNew] as List,
                "test-env",
                contractValidator
            )
        then:
            !validationResult.containsKey(providerOld.getId());
            !validationResult.containsKey(consumerOld.getId());
            validationResult.containsKey(providerNew.getId());
            validationResult.containsKey(consumerNew.getId());

    }
}
