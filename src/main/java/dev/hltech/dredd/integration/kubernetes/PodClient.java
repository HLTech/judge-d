package dev.hltech.dredd.integration.kubernetes;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

public interface PodClient {

    @RequestMapping(value = "/documentation/api-docs", method = RequestMethod.GET)
    ResponseEntity<String> getSwagger(URI uri);

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    ResponseEntity<JsonNode> getInfo();
}
