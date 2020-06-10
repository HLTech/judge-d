package com.hltech.judged.server.interfaces.rest.contracts


import spock.lang.Specification
import spock.lang.Subject;

class ContractsMapperUT extends Specification {

    @Subject
    def mapper = new ContractsMapper()

    def 'should correctly map from expectations form'() {
        given:
            def expectationsForm = generateExpectationsForm()

        expect:
            mapper.mapExpectationsForm(expectationsForm) == generateExpectations()
    }

    def 'should correctly map from capabilities form'() {
        given:
            def capabilitiesForm = generateCapabilitiesForm()

        expect:
            mapper.mapCapabilitiesForm(capabilitiesForm) == generateCapabilities()
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
        ['service': ['rest': generateContract()]]
    }

    def generateCapabilities() {
        ['rest': generateContract()]
    }

    def generateContract() {
        new com.hltech.judged.server.domain.contracts.ServiceContracts.Contract('value', 'mime-type')
    }

    def generateServiceContracts() {
        new com.hltech.judged.server.domain.contracts.ServiceContracts(
            'name',
            'version',
            ['rest' : new com.hltech.judged.server.domain.contracts.ServiceContracts.Contract('value', 'mime-type')],
            ['provider' : ['rest' : new com.hltech.judged.server.domain.contracts.ServiceContracts.Contract('value', 'mime-type')]]
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
