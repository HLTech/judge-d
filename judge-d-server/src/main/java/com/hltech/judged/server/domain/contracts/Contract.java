package com.hltech.judged.server.domain.contracts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Contract {
    private final String value;
    private final String mimeType;
}
