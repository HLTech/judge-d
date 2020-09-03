package com.hltech.judged.server.domain.contracts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Expectation {
    private final String provider;
    private final String protocol;
    private final Contract contract;
}
