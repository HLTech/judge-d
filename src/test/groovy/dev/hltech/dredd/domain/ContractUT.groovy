package dev.hltech.dredd.domain

import dev.hltech.dredd.domain.environment.InMemoryEnvironmentRepository
import dev.hltech.dredd.domain.contracts.InMemoryServiceContractsRepository
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.environment.EnvironmentAggregate
import dev.hltech.dredd.domain.validation.ping.PingContractValidatorFactory
import spock.lang.Specification

import static com.google.common.collect.Sets.newHashSet

class ContractUT extends Specification {

    def servicesRepository = new InMemoryServiceContractsRepository()
    def environmentRepository = new InMemoryEnvironmentRepository()
    def reverseContractFactory = new PingContractValidatorFactory()


    def 'should return empty list of validation reports when validate capabilities given there are no consumers on the env'(){
        given:
            def environment = environmentRepository.persist(new EnvironmentAggregate("SIT", newHashSet()))
            def dredd = new Dredd(servicesRepository, environmentRepository)
        when:
            def validationReport = dredd.validate("SIT", reverseContractFactory.createCapabilities("provider", "654321"))
        then:
            validationReport.size() == 0
    }

    def 'should return proper validation report when validate capabilities given there is one consumer on the env'(){
        given:
            def environment = environmentRepository.persist(new EnvironmentAggregate("SIT", newHashSet(new EnvironmentAggregate.ServiceVersion("consumer", "1.0"))))
            def consumer = servicesRepository.persist(new ServiceContracts(
                "consumer",
                "1.0",
                [:],
                ["provider": ["ping": "123456"]]
            ))
            def dredd = new Dredd(servicesRepository, environmentRepository)
        when:
            def validationReport = dredd.validate("SIT", reverseContractFactory.createCapabilities("provider", "654321"))
        then:
            validationReport.size() == 1

    }

}
