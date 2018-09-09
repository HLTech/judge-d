package dev.hltech.dredd.interfaces.rest.validation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ConsumerAndProviderDto {

    private String consumerName;
    private String consumerVersion;
    private String providerName;
    private String providerVersion;

    @Override
    public String toString() {
        return this.consumerName + ':' + this.consumerVersion + "->" + this.providerName + ':' + this.providerVersion;
    }
}
