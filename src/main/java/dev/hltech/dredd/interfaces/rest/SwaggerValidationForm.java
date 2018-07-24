package dev.hltech.dredd.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;


@Data
public class SwaggerValidationForm {

    private String providerName;
    private ObjectNode swagger;

}
