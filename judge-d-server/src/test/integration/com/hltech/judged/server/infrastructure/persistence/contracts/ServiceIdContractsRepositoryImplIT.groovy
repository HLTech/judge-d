package com.hltech.judged.server.infrastructure.persistence.contracts

import com.hltech.judged.server.domain.ServiceId
import com.hltech.judged.server.domain.contracts.Capability
import com.hltech.judged.server.domain.contracts.Contract
import com.hltech.judged.server.domain.contracts.Expectation
import com.hltech.judged.server.domain.contracts.ServiceContracts
import com.hltech.judged.server.domain.contracts.ServiceContractsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import javax.persistence.NoResultException

import static java.util.function.Function.identity
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["management.port=0"])
@ActiveProfiles("test")
@Transactional
class ServiceIdContractsRepositoryImplIT extends Specification {

    @Autowired
    private ServiceContractsRepository repository

    def 'should find what was saved'() {
        given:
            def serviceContracts = new ServiceContracts(
                new ServiceId('provider', '1.0'),
                [new Capability('ping', new Contract('654321', MediaType.APPLICATION_JSON_VALUE))],
                [new Expectation('some-other-provider', 'ping', new Contract("098765", MediaType.APPLICATION_JSON_VALUE))]
            )
            repository.persist(serviceContracts)
        when:
            def retrieved = repository.findOne(new ServiceId(serviceContracts.name, serviceContracts.version))
        then:
            retrieved.get().id.name == 'provider'
            retrieved.get().id.version == '1.0'
            retrieved.isPresent()
            with(retrieved.get()) {
                getMappedCapabilities("ping", identity()).get() == "654321"
                getMappedExpectations('some-other-provider', "ping", identity()).get() == "098765"
            }
    }

    def 'should find all persisted service names'() {
        given:
            def s1 = repository.persist(new ServiceContracts(new ServiceId(randomAlphabetic(10), "1.0"), [], []))
            def s2 = repository.persist(new ServiceContracts(new ServiceId(randomAlphabetic(10), "1.0"), [], []))
        when:
            def serviceNames = repository.getServiceNames()
        then:
            serviceNames.contains(s1.name)
            serviceNames.contains(s2.name)
    }

    def 'should find all persisted versions of a service'() {
        given:
            def serviceName = randomAlphabetic(10)
            def s1 = repository.persist(new ServiceContracts(new ServiceId(serviceName, randomAlphabetic(5)), [], []))
            def s2 = repository.persist(new ServiceContracts(new ServiceId(serviceName, randomAlphabetic(5)), [], []))
        when:
            def serviceContracts = repository.findAllByServiceName(serviceName)
        then:
            serviceContracts.size() == 2
            serviceContracts.contains(s1)
            serviceContracts.contains(s2)
    }

    def 'should find persisted service by name'() {
        given:
            def serviceName = randomAlphabetic(10)
            def s1 = repository.persist(new ServiceContracts(new ServiceId(serviceName, randomAlphabetic(5)), [], []))
        when:
            String service = repository.getService(serviceName)
        then:
            service == s1.name
    }

    def 'should throw exception when service not found by name'() {
        when:
            repository.getService(randomAlphabetic(7))
        then:
            thrown NoResultException
    }

    def 'should return rest capabilities for given protocol'() {
        given:
        def serviceName = randomAlphabetic(10)
        def version = randomAlphabetic(5)
        def capabilities = [new Capability("protocol", new Contract("capabilities", MediaType.APPLICATION_JSON_VALUE))]
        def s1 = repository.persist(new ServiceContracts(new ServiceId(serviceName, version), capabilities, []))

        when:
        def capability = repository.findCapabilityByServiceIdProtocol(new ServiceId(serviceName, version), "protocol");
        then:
        capability.isPresent()
        capability.get().value == 'capabilities'
        capability.get().mimeType == MediaType.APPLICATION_JSON_VALUE

    }

}
