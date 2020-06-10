package com.hltech.judged.server.interfaces.rest.environment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceForm {

    private String name;
    private String version;

}
