package com.hltech.judged.server.interfaces.rest.contracts

import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import spock.lang.Specification

class ServiceContractsFormUT extends Specification {

    def 'should correctly map from expectations form'() {
        given:
            def serviceContractForm =  new ServiceContractsForm(generateCapabilitiesForm(), generateExpectationsForm())

        when:
            def domainServiceContract = serviceContractForm.toDomain('service', 'v1')

        then:
             def expectedExpectation = generateExpectations()
             with(domainServiceContract.expectations[0]) {
                 provider == expectedExpectation.provider
                 protocol == expectedExpectation.protocol
                 contract.value == expectedExpectation.contract.value
                 contract.mimeType == expectedExpectation.contract.mimeType
             }
        and:
            def expectedCapabilities = generateCapabilities()
            with(domainServiceContract.capabilities[0]) {
                protocol == expectedCapabilities.protocol
                contract.value == expectedCapabilities.contract.value
                contract.mimeType == expectedCapabilities.contract.mimeType

            }
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
}
