package com.hltech.judged.server.interfaces.rest.validation

import com.hltech.judged.server.domain.JudgeDApplicationService
import com.hltech.judged.server.domain.ServiceVersion
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import com.hltech.judged.server.domain.validation.EnvironmentValidatorResult
import com.hltech.judged.server.domain.validation.InterfaceContractValidator
import com.hltech.judged.server.interfaces.rest.RequestValidationException
import com.hltech.judged.server.interfaces.rest.ResourceNotFoundException
import org.assertj.core.util.Lists
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class ValidationControllerUT extends Specification {

    def serviceContractsRepository = Mock(ServiceContractsRepository)
    def validator1 = Mock(InterfaceContractValidator)
    def validator2 = Mock(InterfaceContractValidator)
    def judgeD = Mock(JudgeDApplicationService)

    def 'should validate against every validator available'() {
        given:
            def env = "test-env"
            def env2 = "test-env-2"
            def sc = new ServiceContracts(new ServiceVersion('serviceName', '1.0'), [], []
            )
            def validatorResult = new EnvironmentValidatorResult("ping", newArrayList(), newArrayList())
        when:
            def controller = new ValidationController(judgeD, serviceContractsRepository, [validator1, validator2] as List)
            controller.validateAgainstEnvironments(sc.getName(), sc.getVersion(), Lists.newArrayList(env, env2))
        then:
            1 * serviceContractsRepository.findOne(new ServiceVersion(sc.name, sc.version)) >> Optional.of(sc)
            1 * judgeD.validateServiceAgainstEnvironments(sc, Lists.newArrayList(env, env2), validator1) >> validatorResult
            1 * judgeD.validateServiceAgainstEnvironments(sc, Lists.newArrayList(env, env2), validator2) >> validatorResult
    }

    def 'should throw NotFound exception when validate service against env given contracts for given service have not been registered'() {
        given:
            def env = "test-env"
        when:
            def controller = new ValidationController(judgeD, serviceContractsRepository, [validator1, validator2] as List)
            controller.validateAgainstEnvironments("some-service", "service-version", Lists.newArrayList(env))
        then:
            1 * serviceContractsRepository.findOne(new ServiceVersion("some-service", "service-version")) >> Optional.empty()
            thrown ResourceNotFoundException
    }

    def "should throw 400 when invalid format of service id" (){
        given:
            def controller = new ValidationController(judgeD, serviceContractsRepository, [validator1, validator2] as List)
        when:
            controller.validateAgainstEnvironments(["service.version", "service-without-version"] as List, "some-env")
        then:
            thrown RequestValidationException

    }

    def "should throw 400 when contracts not registered" (){
        given:
            def version = new ServiceVersion("service", "version")
            serviceContractsRepository.findOne(version) >> Optional.empty()
            def controller = new ValidationController(judgeD, serviceContractsRepository, [validator1, validator2] as List)
        when:
            controller.validateAgainstEnvironments(["service:version"] as List, "some-env")
        then:
            1 * serviceContractsRepository.findOne(version) >> Optional.empty()
            thrown RequestValidationException

    }

}
