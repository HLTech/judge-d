package dev.hltech.dredd.interfaces.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PactValidationForm {

    private List<ObjectNode> pacts;

}
