package dev.hltech.dredd.domain

import au.com.dius.pact.model.RequestResponsePact
import dev.hltech.dredd.domain.environment.Environment
import dev.hltech.dredd.domain.environment.StaticEnvironment

import static au.com.dius.pact.model.PactReader.loadPact
import static com.google.common.collect.Lists.newArrayList
import static com.google.common.io.ByteStreams.toByteArray

class Fixtures {

    static Environment environment(){
        return StaticEnvironment.builder()
            .withProvider(
            "dde-instruction-gateway",
            "1.0",
            new String(toByteArray(getClass().getResourceAsStream("/dde-instruction-gateway-swagger.json")))
        )
            .withConsumer(
            "frontend",
            "1.0",
            newArrayList(
                (RequestResponsePact) loadPact(getClass().getResourceAsStream("/pact-frontend-to-dde-instruction-gateway.json"))
            )
        )
            .build()
    }
}
