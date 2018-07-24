package dev.hltech.dredd.domain

import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.environment.EnvironmentAggregate
import dev.hltech.dredd.domain.validation.JudgeD
import dev.hltech.dredd.domain.validation.ping.PingContractValidator
import spock.lang.Specification

import static com.google.common.collect.Sets.newHashSet

class CapabilitiesUT extends Specification {

    def servicesRepository = new InMemoryServiceContractsRepository()
    def environmentRepository = new InMemoryEnvironmentRepository()
    def pingContractFactory = new PingContractValidator()

    def 'should return empty list of validation reports when validate capabilities given there are no consumers on the env'(){
        given:
            environmentRepository.persist(new EnvironmentAggregate("SIT", newHashSet()))
            def dredd = new JudgeD(environmentRepository, servicesRepository)
        when:
            def validationReport = dredd.createContractValidator("SIT", pingContractFactory).validateCapabilities("provider", "654321")
        then:
            validationReport.size() == 0
    }

    def 'should return proper validation report when validate capabilities given there is one consumer on the env'(){
        given:
            environmentRepository.persist(new EnvironmentAggregate("SIT", newHashSet(new EnvironmentAggregate.ServiceVersion("consumer", "1.0"))))
            def consumer = servicesRepository.persist(new ServiceContracts(
                "consumer",
                "1.0",
                [:],
                ["provider": ["ping": "123456"]]
            ))
            def dredd = new JudgeD(environmentRepository, servicesRepository)
        when:
            def validationReport = dredd.createContractValidator("SIT", pingContractFactory).validateCapabilities("provider", "654321")
        then:
            validationReport.size() == 1

    }

}
