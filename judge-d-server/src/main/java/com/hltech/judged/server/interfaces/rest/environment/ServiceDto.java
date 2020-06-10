package com.hltech.judged.server.interfaces.rest.environment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceDto {

    private String name;
    private String version;
}
