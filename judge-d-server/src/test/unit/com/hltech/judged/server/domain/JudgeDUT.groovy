package com.hltech.judged.server.domain

import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.InMemoryServiceContractsRepository
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.InMemoryEnvironmentRepository
import com.hltech.judged.server.domain.environment.Space
import com.hltech.judged.server.domain.validation.InterfaceContractValidator
import com.hltech.judged.server.domain.validation.ping.PingContractValidator
import com.hltech.judged.server.interfaces.rest.ResourceNotFoundException
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Subject

import static org.assertj.core.util.Lists.newArrayList

class JudgeDUT extends Specification {

    def serviceContractsRepository = new InMemoryServiceContractsRepository()
    def environmentRepository = new InMemoryEnvironmentRepository()
    def contractValidator = new PingContractValidator()
    def mockedValidator = Mock(InterfaceContractValidator)

    @Subject
    def judgeD = new JudgeDApplicationService(environmentRepository, serviceContractsRepository, [contractValidator, mockedValidator])

    def 'should validate expectations against environment without provider'() {
        given:
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('validated-consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment('test-env', [] as Set))

        when:
            def evrs = judgeD.validateServiceAgainstEnvironments(
                consumer.id,
                ["test-env"] as List
            )
        then:
            evrs[0].getCapabilitiesValidationResults().size() == 0
            evrs[0].getExpectationValidationResults().size() == 1
    }

    def 'should validate expectations against environment with provider'() {
        given:
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('validated-consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [new Space('def', [new ServiceId("provider", "1.0")] as Set)] as Set))
        when:
            def evrs = judgeD.validateServiceAgainstEnvironments(
                consumer.id,
                newArrayList("test-env"))
        then:
            evrs[0].getCapabilitiesValidationResults().size() == 0
            evrs[0].getExpectationValidationResults().size() == 1

    }

    def 'should validate capabilities against environment with consumer'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider-x', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '1.0'),
                [],
                [new Expectation('provider-x', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [new Space('def', [new ServiceId("consumer", "1.0")] as Set)] as Set))
        when:
            def evrs = judgeD.validateServiceAgainstEnvironments(
                provider.id,
                newArrayList("test-env"))
        then:
            evrs[0].getCapabilitiesValidationResults().size() == 1
            evrs[0].getExpectationValidationResults().size() == 0
    }

    def 'should validate capabilities against environment without a consumer'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider-x', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            environmentRepository.persist(new Environment("test-env", [new Space('def', [new ServiceId("consumer", "1.0")] as Set)] as Set))
        when:
            def evrs = judgeD.validateServiceAgainstEnvironments(
                provider.id,
                newArrayList("test-env"))
        then:
            evrs[0].getCapabilitiesValidationResults().size() == 0
            evrs[0].getExpectationValidationResults().size() == 0
    }

    def 'should validate capabilities against multiple environments'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider-x', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '1.0'),
                [],
                [new Expectation('provider-x', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer2', '1.0'),
                [],
                [new Expectation('provider-x', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [new Space('def', [new ServiceId("consumer", "1.0")] as Set)] as Set))
            environmentRepository.persist(new Environment("test-env2", [new Space('def', [new ServiceId("consumer2", "1.0")] as Set)] as Set))
        when:
            def evrs = judgeD.validateServiceAgainstEnvironments(
                provider.id,
                newArrayList("test-env", "test-env2"))
        then:
            evrs[0].getCapabilitiesValidationResults().size() == 2
            evrs[0].getExpectationValidationResults().size() == 0
    }

    def 'should validate contracts of set of services against empty environment'() {
        given:
            def provider = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumer = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            environmentRepository.persist(new Environment("test-env", [] as Set))
        when:
            def validationResult = judgeD.validatedServicesAgainstEnvironment(
                [provider.id, consumer.id] as List,
                "test-env")
        then:
            validationResult.containsKey(provider.getId())
            validationResult.containsKey(consumer.getId())

    }

    def 'should validate contracts of set of services against env containing all services but with different version'() {
        given:
            def providerOld = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider', '1.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumerOld = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '1.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('1233456', MediaType.APPLICATION_JSON_VALUE))]
            ))
            def providerNew = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('provider', '2.0'),
                [new Capability('ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))],
                []
            ))
            def consumerNew = serviceContractsRepository.persist(new ServiceContracts(
                new ServiceId('consumer', '2.0'),
                [],
                [new Expectation('provider', 'ping', new Contract('123456', MediaType.APPLICATION_JSON_VALUE))]
            ))
        environmentRepository.persist(new Environment("test-env", [new Space('def', [
            new ServiceId(providerOld.id.name, providerOld.id.version),
            new ServiceId(consumerOld.id.name, consumerOld.id.version)
        ] as Set)] as Set))
        when:
            def validationResult = judgeD.validatedServicesAgainstEnvironment(
                [providerNew.id, consumerNew.id] as List,
                "test-env")
        then:
            !validationResult.containsKey(providerOld.getId())
            !validationResult.containsKey(consumerOld.getId())
            validationResult.containsKey(providerNew.getId())
            validationResult.containsKey(consumerNew.getId())
    }

    def 'should throw NotFound exception when validate service against env given contracts for given service have not been registered'() {
        when:
            judgeD.validateServiceAgainstEnvironments(new ServiceId('n', 'v'), ["test-env"] as List)

        then:
            thrown ResourceNotFoundException
    }

    def 'should return two services given agents from two different spaces saved before'(){
        given:
            def sv1 = new ServiceId("service1", "version1")
            def sv2 = new ServiceId("service2", "version2")

        when:
            judgeD.overwriteEnvironment("env", null, [sv1] as Set)
            judgeD.overwriteEnvironment("env", "space", [sv2] as Set)

        then:
            environmentRepository.find("env").get().allServices.size() == 2
    }

    def 'should override default space only given additional non-empty space exists and both are overwritten'(){
        given:
            def sv1 = new ServiceId("service1", "version1")
            def sv2 = new ServiceId("service2", "version2")
            def sv3 = new ServiceId("service3", "version3")

        when:
            judgeD.overwriteEnvironment("env", null, [sv1] as Set)
            judgeD.overwriteEnvironment("env", null, [sv2] as Set)
            judgeD.overwriteEnvironment("env", "space", [sv3] as Set)

        then:
            def environment = environmentRepository.find("env").get()

            environment.allServices.size() == 2
            environment.allServices.any{
                it.name == "service2" && it.version == "version2"
            }
            environment.allServices.any{
                it.name == "service3" && it.version == "version3"
            }
    }
}
