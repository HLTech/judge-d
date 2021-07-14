package com.hltech.judged.server.interfaces.rest.interrelationship

import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import com.hltech.judged.server.domain.environment.Environment
import com.hltech.judged.server.domain.environment.EnvironmentRepository
import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.domain.environment.Space
import com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto
import spock.lang.Specification
import spock.lang.Subject

class InterrelationshipControllerUT extends Specification {

    def environmentRepository = Mock(EnvironmentRepository)

    def serviceContractsRepository = Mock(ServiceContractsRepository)

    @Subject
    def controller = new InterrelationshipController(environmentRepository, serviceContractsRepository)

    def "should return 200 when getting interrelationship for any environment"() {
        given:
            def envName = 'SIT'
            def services = [new ServiceId('1', '1'), new ServiceId('2', '2')] as Set
            def environment = new Environment(envName, [new Space('def', services)] as Set)

            1 * environmentRepository.get(envName) >> environment
            1 * serviceContractsRepository.findOne(new ServiceId('1', '1')) >> Optional.of(createServiceContracts('1', '1'))
            1 * serviceContractsRepository.findOne(new ServiceId('2', '2')) >> Optional.of(createServiceContracts('2', '2'))

        when:
            def result = controller.getInterrelationship(envName)

        then:
            result.getEnvironment() == envName
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
            new ServiceId(name, version),
            [new Capability('jms', new Contract('contract-jms', 'json'))],
            [new Expectation('prov', 'rest', new Contract('contract-rest', 'json'))])
    }
}
