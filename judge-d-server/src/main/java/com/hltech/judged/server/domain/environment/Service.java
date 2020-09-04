package com.hltech.judged.server.domain.environment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Service {
    private final String name;
    private final String version;
}
