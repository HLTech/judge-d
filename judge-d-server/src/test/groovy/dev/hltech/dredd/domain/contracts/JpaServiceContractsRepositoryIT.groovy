package dev.hltech.dredd.domain.contracts

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static java.util.function.Function.identity
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["management.port=0"])
@ActiveProfiles("test-integration")
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

}
