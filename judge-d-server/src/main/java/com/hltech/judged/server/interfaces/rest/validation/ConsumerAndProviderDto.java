package com.hltech.judged.server.interfaces.rest.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class ConsumerAndProviderDto {

    private final String consumerName;
    private final String consumerVersion;
    private final String providerName;
    private final String providerVersion;

    @Override
    public String toString() {
        return this.consumerName + ':' + this.consumerVersion + "->" + this.providerName + ':' + this.providerVersion;
    }
}
