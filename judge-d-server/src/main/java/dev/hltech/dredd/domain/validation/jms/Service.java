package dev.hltech.dredd.domain.validation.jms;

import lombok.Data;

@Data
public class Service {

    private final String name;
    private final Capabilities capabilities;
    private final Expectations expectations;
}
