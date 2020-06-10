package com.hltech.judged.server.interfaces.rest.interrelationship


import spock.lang.Specification
import spock.lang.Subject

class InterrelationshipControllerUT extends Specification {

    def environmentRepository = Mock(com.hltech.judged.server.domain.environment.EnvironmentRepository)

    def serviceContractsRepository = Mock(com.hltech.judged.server.domain.contracts.ServiceContractsRepository)

    def contractsMapper = new com.hltech.judged.server.interfaces.rest.contracts.ContractsMapper()

    @Subject
    def controller = new InterrelationshipController(environmentRepository, serviceContractsRepository, contractsMapper)

    def "should return 200 when getting interrelationship for any environment"() {
        given:
            def env = 'SIT'
            def services = [new com.hltech.judged.server.domain.ServiceVersion('1', '1'), new com.hltech.judged.server.domain.ServiceVersion('2', '2')] as Set
            def environment = new com.hltech.judged.server.domain.environment.EnvironmentAggregate(env, services)

            1 * environmentRepository.get(env) >> environment
            1 * serviceContractsRepository.findOne(new com.hltech.judged.server.domain.ServiceVersion('1', '1')) >> Optional.of(createServiceContracts('1', '1'))
            1 * serviceContractsRepository.findOne(new com.hltech.judged.server.domain.ServiceVersion('2', '2')) >> Optional.of(createServiceContracts('2', '2'))

        when:
            def result = controller.getInterrelationship(env)

        then:
            result.getEnvironment() == env
            result.getServiceContracts().size() == 2
            result.getServiceContracts().any({ it ->
                it.name == '1'
                it.version == '1'
                it.expectations == ['prov': ['rest': new com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto.ContractDto('contract-rest', 'json')]]
                it.capabilities == ['jms': new com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto.ContractDto('contract-jms', 'json')]
            })
            result.getServiceContracts().any({
                it.name == '2'
                it.version == '2'
                it.expectations == ['prov': ['rest': new com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto.ContractDto('contract-rest', 'json')]]
                it.capabilities == ['jms': new com.hltech.judged.server.interfaces.rest.contracts.ServiceContractsDto.ContractDto('contract-jms', 'json')]
            })
    }

    def createServiceContracts(def name, def version) {
        new com.hltech.judged.server.domain.contracts.ServiceContracts(
            'id': new com.hltech.judged.server.domain.ServiceVersion(name, version),
            'capabilitiesPerProtocol': ['jms': new com.hltech.judged.server.domain.contracts.ServiceContracts.Contract('contract-jms', 'json')],
            'expectations': [(new com.hltech.judged.server.domain.contracts.ServiceContracts.ProviderProtocol('prov', 'rest')): new com.hltech.judged.server.domain.contracts.ServiceContracts.Contract('contract-rest', 'json')])
    }
}
