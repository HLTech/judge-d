package com.hltech.judged.server.domain.environment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Space {
    private final String name;
    private final Set<Service> services;
}
