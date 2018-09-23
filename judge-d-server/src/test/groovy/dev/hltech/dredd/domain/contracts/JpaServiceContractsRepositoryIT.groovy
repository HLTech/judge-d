package dev.hltech.dredd.domain.contracts

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static java.util.function.Function.identity
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["management.port=0"])
@ActiveProfiles("test-integration")
@Transactional
class JpaServiceContractsRepositoryIT extends Specification {

    @Autowired
    private ServiceContractsRepository repository

    def 'should find what was saved'() {
        given:
            def serviceContracts = new ServiceContracts(
                "provider",
                "1.0",
                ['ping': '654321'],
                ['some-other-provider': ['ping': '098765']]
            )
            repository.persist(serviceContracts)
        when:
            def retrieved = repository.find(serviceContracts.name, serviceContracts.version)
        then:
            retrieved.isPresent() == true
            with(retrieved.get()) {
                getCapabilities("ping", identity()).get() == "654321"
                getExpectations('some-other-provider', "ping", identity()).get() == "098765"
            }
    }

    def 'should find all persisted service names'() {
        given:
            def s1 = repository.persist(new ServiceContracts(randomAlphabetic(10), "1.0", [:], [:]))
            def s2 = repository.persist(new ServiceContracts(randomAlphabetic(10), "1.0", [:], [:]))
        when:
            def serviceNames = repository.getServiceNames()
        then:
            serviceNames.contains(s1.name)
            serviceNames.contains(s2.name)
    }

    def 'should find all persisted versions of a service'() {
        given:
            def serviceName = randomAlphabetic(10)
            def s1 = repository.persist(new ServiceContracts(serviceName, randomAlphabetic(5), [:], [:]))
            def s2 = repository.persist(new ServiceContracts(serviceName, randomAlphabetic(5), [:], [:]))
        when:
            def serviceContracts = repository.find(serviceName)
        then:
            serviceContracts.size() == 2
            serviceContracts.contains(s1)
            serviceContracts.contains(s2)
    }

}
