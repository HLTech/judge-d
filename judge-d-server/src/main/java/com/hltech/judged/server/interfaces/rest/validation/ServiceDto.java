package com.hltech.judged.server.interfaces.rest.validation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceDto {

    private String name;
    private String version;
}
