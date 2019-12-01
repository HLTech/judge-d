package dev.hltech.dredd.interfaces.rest.interrelationship

import dev.hltech.dredd.domain.contracts.ServiceContracts
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository
import dev.hltech.dredd.domain.environment.EnvironmentAggregate
import dev.hltech.dredd.domain.environment.EnvironmentRepository
import dev.hltech.dredd.domain.ServiceVersion
import dev.hltech.dredd.interfaces.rest.contracts.ContractsMapper
import dev.hltech.dredd.interfaces.rest.contracts.ServiceContractsDto
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
            def services = [new ServiceVersion('1', '1'), new ServiceVersion('2', '2')]

            1 * environmentRepository.get(env) >> new EnvironmentAggregate('name': env, 'serviceVersions': services)
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
            'id': new ServiceVersion(name, version),
            'capabilitiesPerProtocol': ['jms': new ServiceContracts.Contract('contract-jms', 'json')],
            'expectations': [(new ServiceContracts.ProviderProtocol('prov', 'rest')): new ServiceContracts.Contract('contract-rest', 'json')])
    }
}
