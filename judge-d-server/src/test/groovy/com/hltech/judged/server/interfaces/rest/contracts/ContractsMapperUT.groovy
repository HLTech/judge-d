package com.hltech.judged.server.interfaces.rest.contracts

import com.hltech.judged.server.domain.ServiceVersion
import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.ServiceContracts
import spock.lang.Specification
import spock.lang.Subject

class ContractsMapperUT extends Specification {

    @Subject
    def mapper = new ContractsMapper()

    def 'should correctly map from expectations form'() {
        given:
            def expectationsForm = generateExpectationsForm()

        when:
            def expectations = mapper.mapExpectationsForm(expectationsForm)

        then:
            def expectedExpectation = generateExpectations()
            expectations[0].provider == expectedExpectation.provider
            expectations[0].protocol == expectedExpectation.protocol
            expectations[0].contract.value == expectedExpectation.contract.value
            expectations[0].contract.mimeType == expectedExpectation.contract.mimeType
    }

    def 'should correctly map from capabilities form'() {
        given:
            def capabilitiesForm = generateCapabilitiesForm()

        when:
            def capabilities = mapper.mapCapabilitiesForm(capabilitiesForm)

        then:
            def expectedCapabilities = generateCapabilities()
            capabilities[0].protocol == expectedCapabilities.protocol
            capabilities[0].contract.value == expectedCapabilities.contract.value
            capabilities[0].contract.mimeType == expectedCapabilities.contract.mimeType
    }

    def 'should correctly map to dto'() {
        given:
            def serviceContracts = generateServiceContracts()

        expect:
            mapper.toDto(serviceContracts) == generateServiceContractsDto()
    }

    def generateExpectationsForm() {
        ['service' : ['rest' : generateContractForm()]]
    }

    def generateCapabilitiesForm() {
        ['rest' : generateContractForm()]
    }

    def generateContractForm() {
        new ServiceContractsForm.ContractForm('value', 'mime-type')
    }

    def generateExpectations() {
        new Expectation('service', 'rest', generateContract())
    }

    def generateCapabilities() {
        new Capability('rest', generateContract())
    }

    def generateContract() {
        new Contract('value', 'mime-type')
    }

    def generateServiceContracts() {
        new ServiceContracts(
            new ServiceVersion('name', 'version'),
            [new Capability('rest', new Contract('value', 'mime-type'))],
            [new Expectation('provider', 'rest', new Contract('value', 'mime-type'))]
        )
    }

    def generateServiceContractsDto() {
        new ServiceContractsDto(
            'name',
            'version',
            ['rest' : new ServiceContractsDto.ContractDto('value', 'mime-type')],
            ['provider' : ['rest' : new ServiceContractsDto.ContractDto('value', 'mime-type')]]
        )
    }
}
