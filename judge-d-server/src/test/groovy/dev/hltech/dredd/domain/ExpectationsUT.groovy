package dev.hltech.dredd.domain

import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.environment.EnvironmentAggregate
import dev.hltech.dredd.domain.validation.InterfaceContractValidator
import dev.hltech.dredd.domain.validation.JudgeD
import dev.hltech.dredd.domain.validation.ProviderNotAvailableException
import dev.hltech.dredd.domain.validation.ping.PingContractValidator
import spock.lang.Specification

import static com.google.common.collect.Sets.newHashSet

import static dev.hltech.dredd.domain.environment.EnvironmentAggregate.ServiceVersion
import static dev.hltech.dredd.domain.validation.InterfaceContractValidator.InteractionValidationStatus.*

class ExpectationsUT extends Specification{

    def servicesRepository = new InMemoryServiceContractsRepository()
    def environmentRepository = new InMemoryEnvironmentRepository()
    def pingContractValidator = new PingContractValidator()


    def 'should throw ProviderNotFoundException when validate expectations agains env given required provider doesnt exist'() {
        given:
            def environment = environmentRepository.persist(new EnvironmentAggregate("SIT", newHashSet(new ServiceVersion("some-other-provider", "1"))))
            def contractValidator = new JudgeD( environmentRepository, servicesRepository).createContractValidator(environment.name, pingContractValidator)
        when:
            contractValidator.validateExpectations("provider", pingContractValidator.asExpectations("123456"))
        then:
            thrown ProviderNotAvailableException
    }

    def 'should return report with one invalid interaction when validate expectations given required provider exists on env but has not been registered'(){
        given:
            def environment = environmentRepository.persist(new EnvironmentAggregate("SIT", newHashSet(new ServiceVersion("provider", "1"))))
            def contractValidator = new JudgeD( environmentRepository, servicesRepository).createContractValidator(environment.name,pingContractValidator)
        when:
            def validationReport = contractValidator.validateExpectations("provider", pingContractValidator.asExpectations("123456"))
        then:
            validationReport.size() == 1
            with (validationReport.get(0)) {
                interactionValidationResults.size() == 1
                interactionValidationResults.get(0).status == FAILED
            }
    }

    def 'should delete to contract validator when validate expectation given'() {
        given:
            def registeredProvider = servicesRepository.persist(
                new ServiceContracts("some-provider", "1.0",
                    ["ping": "654321"],
                    [:]
                )
            )
            def environment = environmentRepository.persist(new EnvironmentAggregate(
                "SIT",
                newHashSet(new ServiceVersion(registeredProvider.name, registeredProvider.version))
            ))
            def contractValidator = new JudgeD( environmentRepository, servicesRepository).createContractValidator(environment.name, pingContractValidator)
        when:
            def validationReport = contractValidator.validateExpectations(registeredProvider.getName(), pingContractValidator.asExpectations("654321"))
        then:
            validationReport.size() == 1
            with(validationReport.get(0)) {
                providerName == registeredProvider.getName()
                providerVersion == registeredProvider.getVersion()

                interactionValidationResults.size() == 1
                interactionValidationResults.get(0).status == OK
            }
    }
}
