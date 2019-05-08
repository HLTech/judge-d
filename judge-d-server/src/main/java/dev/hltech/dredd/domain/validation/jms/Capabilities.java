package dev.hltech.dredd.domain.validation.jms;

import lombok.Data;

import java.util.List;

@Data
public class Capabilities {
    private final List<Contract> contracts;
}
