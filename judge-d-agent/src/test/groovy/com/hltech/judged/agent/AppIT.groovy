package com.hltech.judged.agent

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["management.port=0"])
@ActiveProfiles(["test-integration", "kubernetes"])
class AppIT extends Specification {

    def test() {
        when:
        def a = 2
        then:
        a == 2
    }

}
