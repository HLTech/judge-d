package dev.hltech.dredd.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.List;

@Data
public class PactValidationForm {

    private List<ObjectNode> pacts;

}
