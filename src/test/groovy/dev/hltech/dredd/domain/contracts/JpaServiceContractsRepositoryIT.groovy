package dev.hltech.dredd.domain.contracts

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@AutoConfigureWireMock(port = 0, httpsPort = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-integration")
class JpaServiceContractsRepositoryIT extends Specification {

    @Autowired
    private ServiceContractsRepository repository

    def 'should find what was saved'() {
        given:
            def serviceContracts = new ServiceContracts(
                "provider",
                "1.0",
                ['reverse':'654321'],
                ['some-other-provider': ['reverse' : '098765']]
            )
            repository.persist(serviceContracts)
        when:
            def retrieved = repository.find(serviceContracts.name, serviceContracts.version)
        then:
            retrieved.isPresent() == true
            with (retrieved.get()) {
                getCapabilities("reverse").get() == "654321"
                getExpectations('some-other-provider', "reverse").get() == "098765"
            }
    }

}
