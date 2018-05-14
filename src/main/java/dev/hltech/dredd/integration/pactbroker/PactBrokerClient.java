package dev.hltech.dredd.integration.pactbroker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface PactBrokerClient {

    @RequestMapping(value = "/pacts/provider/{provider}/consumer/{consumer}/version/{consumerVersion}", method = RequestMethod.GET)
    ObjectNode getPact(
        @PathVariable("provider") String providerName,
        @PathVariable("consumer") String consumerName,
        @PathVariable("consumerVersion") String consumerVersion
    );

}
