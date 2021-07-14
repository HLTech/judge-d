package com.hltech.judged.server.interfaces.rest.contracts

import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.ServiceContracts
import spock.lang.Specification

class ServiceContractsDtoUT extends Specification {

    def 'Valid dto should be created from domain object'() {
        given:
            def serviceContracts = generateServiceContracts()

        expect:
            ServiceContractsDto.fromDomain(serviceContracts) == generateServiceContractsDto()
    }


    def generateServiceContracts() {
        new ServiceContracts(
            new ServiceId('name', 'version'),
            [new Capability('rest', new Contract('value', 'mime-type'))],
            [new Expectation('provider', 'rest', new Contract('value', 'mime-type'))]
        )
    }

    def generateServiceContractsDto() {
        new ServiceContractsDto(
            'name',
            'version',
            ['rest': new ServiceContractsDto.ContractDto('value', 'mime-type')],
            ['provider': ['rest': new ServiceContractsDto.ContractDto('value', 'mime-type')]]
        )
    }
}
