package dev.hltech.dredd;

import com.arakelian.docker.junit.DockerRule;
import com.arakelian.docker.junit.model.ImmutableDockerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import dev.hltech.dredd.interfaces.rest.AggregatedValidationReportDto;
import dev.hltech.dredd.interfaces.rest.PactValidationForm;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertNotNull;

public class StaticEnvironmentIT {

    @ClassRule
    public static DockerRule rabbitmq = new DockerRule(
        ImmutableDockerConfig.builder()
            .name("dredd_test_" + randomAlphabetic(10))
            .image("tools-registry.hltech.dev/contract-verifier:0.0.1-SNAPSHOT")
            .ports("8080")
            .addStartedListener(container -> {
                container.waitForPort("8080/tcp");
                container.waitForLog("Started App in");
            }).build()
    );

    private DreddClient dreddClient;

    @Before
    public void setUp() {
        dreddClient = Feign.builder()
            .contract(new JAXRSContract())
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .target(DreddClient.class, "http://localhost:" + rabbitmq.getContainer().getPortBinding("8080/tcp").getPort() + "/");
    }

    @Test
    public void tst() throws IOException {
        List<ObjectNode> pacts = Lists.newArrayList();
        pacts.add((ObjectNode) new ObjectMapper().readTree(getClass().getResourceAsStream("/pact-frontend-to-backend-provider.json")));

        AggregatedValidationReportDto validate = dreddClient.validate(new PactValidationForm(pacts));

        assertNotNull(validate);

    }

}
