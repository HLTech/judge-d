package com.hltech.judged.agent.hltech;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface PodClient {

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    ResponseEntity<JsonNode> getInfo();
}
