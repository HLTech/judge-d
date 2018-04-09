package dev.hltech.dredd.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class PactVerificationForm {

    private List<JsonNode> pacts;

}
