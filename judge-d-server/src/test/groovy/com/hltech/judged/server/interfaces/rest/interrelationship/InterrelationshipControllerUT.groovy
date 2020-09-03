package com.hltech.judged.server.interfaces.rest.interrelationship

import com.hltech.judged.server.domain.ServiceVersion
import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.EnvironmentRepository
import com.hltech.judged.server.interfaces.rest.contracts.ContractsMapper
import com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto
import spock.lang.Specification
import spock.lang.Subject

class InterrelationshipControllerUT extends Specification {

    def environmentRepository = Mock(EnvironmentRepository)

    def serviceContractsRepository = Mock(ServiceContractsRepository)

    def contractsMapper = new ContractsMapper()

    @Subject
    def controller = new InterrelationshipController(environmentRepository, serviceContractsRepository, contractsMapper)

    def "should return 200 when getting interrelationship for any environment"() {
        given:
            def env = 'SIT'
            def services = [new ServiceVersion('1', '1'), new ServiceVersion('2', '2')] as Set
            def environment = new Environment(env, services)

            1 * environmentRepository.get(env) >> environment
            1 * serviceContractsRepository.findOne(new ServiceVersion('1', '1')) >> Optional.of(createServiceContracts('1', '1'))
            1 * serviceContractsRepository.findOne(new ServiceVersion('2', '2')) >> Optional.of(createServiceContracts('2', '2'))

        when:
            def result = controller.getInterrelationship(env)

        then:
            result.getEnvironment() == env
            result.getServiceContracts().size() == 2
            result.getServiceContracts().any({ it ->
                it.name == '1'
                it.version == '1'
                it.expectations == ['prov': ['rest': new ServiceContractsDto.ContractDto('contract-rest', 'json')]]
                it.capabilities == ['jms': new ServiceContractsDto.ContractDto('contract-jms', 'json')]
            })
            result.getServiceContracts().any({
                it.name == '2'
                it.version == '2'
                it.expectations == ['prov': ['rest': new ServiceContractsDto.ContractDto('contract-rest', 'json')]]
                it.capabilities == ['jms': new ServiceContractsDto.ContractDto('contract-jms', 'json')]
            })
    }

    def createServiceContracts(def name, def version) {
        new ServiceContracts(
            new ServiceVersion(name, version),
            [new Capability('jms', new Contract('contract-jms', 'json'))],
            [new Expectation('prov', 'rest', new Contract('contract-rest', 'json'))])
    }
}
