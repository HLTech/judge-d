package dev.hltech.dredd.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SwaggerVerificationForm {

    private String providerName;
    private JsonNode swagger;

}
