package dev.hltech.dredd.interfaces.rest.validation.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwaggerValidationForm {

    @NotNull
    private String providerName;
    private ObjectNode swagger;

}
