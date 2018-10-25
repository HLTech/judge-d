package dev.hltech.dredd.interfaces.rest.validation

import dev.hltech.dredd.domain.JudgeD
import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import dev.hltech.dredd.domain.validation.EnvironmentValidatorResult
import dev.hltech.dredd.domain.validation.InterfaceContractValidator
import dev.hltech.dredd.interfaces.rest.ResourceNotFoundException
import org.assertj.core.util.Lists
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class ValidationControllerUT extends Specification {

    def serviceContractsRepository = Mock(ServiceContractsRepository)
    def validator1 = Mock(InterfaceContractValidator)
    def validator2 = Mock(InterfaceContractValidator)
    def judgeD = Mock(JudgeD)

    def 'should validate against every validator available'() {
        given:
            def env = "test-env"
            def env2 = "test-env-2"
            def sc = new ServiceContracts(
                "serviceName",
                "1.0",
                [:] as Map,
                [:] as Map
            )
            def validatorResult = new EnvironmentValidatorResult("ping", newArrayList(), newArrayList())
        when:
            def controller = new ValidationController(judgeD, serviceContractsRepository, [validator1, validator2] as List)
            def validationReports = controller.validateAgainstEnvironments(sc.getName(), sc.getVersion(), Lists.newArrayList(env, env2))
        then:
            1 * serviceContractsRepository.find(sc.name, sc.version) >> Optional.of(sc)
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
            1 * serviceContractsRepository.find("some-service", "service-version") >> Optional.empty()
            thrown ResourceNotFoundException
    }
}
