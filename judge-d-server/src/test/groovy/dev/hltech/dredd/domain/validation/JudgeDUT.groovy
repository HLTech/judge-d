package dev.hltech.dredd.domain.validation

import dev.hltech.dredd.domain.JudgeD
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.environment.EnvironmentAggregate
import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.domain.validation.ping.PingContractValidator
import org.assertj.core.util.Lists
import org.springframework.http.MediaType
import spock.lang.Specification

class JudgeDUT extends Specification {

    def serviceContractsRepository = new InMemoryServiceContractsRepository();
    def environmentRepository = new InMemoryEnvironmentRepository()
    def judgeD = new JudgeD(environmentRepository, serviceContractsRepository)

    def 'validate expectations against environment without provider'() {
        given:
            def validatedService = serviceContractsRepository.persist(new ServiceContracts(
                "validated-service",
                "1.0",
                [:],
                ["provider": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
            environmentRepository.persist(new EnvironmentAggregate("test-env", [] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                validatedService,
                Lists.newArrayList("test-env"),
                new PingContractValidator()
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 0
            evr.getExpectationValidationResults().size() == 1

    }

    def 'validate expectations against environment with provider '() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                "provider",
                "1.0",
                ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)],
                [:]
            ))
            def validatedService = serviceContractsRepository.persist(new ServiceContracts(
                "validated-service",
                "1.0",
                [:],
                ["provider": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
            environmentRepository.persist(new EnvironmentAggregate("test-env", [new EnvironmentAggregate.ServiceVersion("provider", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                validatedService,
                Lists.newArrayList("test-env"),
                new PingContractValidator()
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 0
            evr.getExpectationValidationResults().size() == 1

    }

    def 'validate capabilities against environment with consumer'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                "provider-x",
                "1.0",
                ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)],
                [:]
            ))
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                "consumer",
                "1.0",
                [:],
                ["provider-x": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
            environmentRepository.persist(new EnvironmentAggregate("test-env", [new EnvironmentAggregate.ServiceVersion("consumer", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                provider,
                Lists.newArrayList("test-env"),
                new PingContractValidator()
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 1
            evr.getExpectationValidationResults().size() == 0
    }

    def 'validate capabilities against environment without a consumer'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                "provider-x",
                "1.0",
                ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)],
                [:]
            ))
            environmentRepository.persist(new EnvironmentAggregate("test-env", [new EnvironmentAggregate.ServiceVersion("consumer", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                provider,
                Lists.newArrayList("test-env"),
                new PingContractValidator()
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 0
            evr.getExpectationValidationResults().size() == 0
    }

    def 'validate capabilities against multiple environments'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                "provider-x",
                "1.0",
                ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)],
                [:]
            ))
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                "consumer",
                "1.0",
                [:],
                ["provider-x": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
            def consumer2 = serviceContractsRepository.persist(new ServiceContracts(
                "consumer2",
                "1.0",
                [:],
                ["provider-x": ["ping": new ServiceContracts.Contract("123456", MediaType.APPLICATION_JSON_VALUE)]]
            ))
            environmentRepository.persist(new EnvironmentAggregate("test-env", [new EnvironmentAggregate.ServiceVersion("consumer", "1.0")] as Set))
            environmentRepository.persist(new EnvironmentAggregate("test-env2", [new EnvironmentAggregate.ServiceVersion("consumer2", "1.0")] as Set))
        when:
            EnvironmentValidatorResult evr = judgeD.validateServiceAgainstEnvironments(
                provider,
                Lists.newArrayList("test-env", "test-env2"),
                new PingContractValidator()
            )
        then:
            evr.getCapabilitiesValidationResults().size() == 2
            evr.getExpectationValidationResults().size() == 0
    }
}
