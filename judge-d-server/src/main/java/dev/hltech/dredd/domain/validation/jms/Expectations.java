package dev.hltech.dredd.domain.validation.jms;

import com.google.common.collect.Multimap;
import lombok.Data;

@Data
public class Expectations {
    private final Multimap<String, Contract> providerNameToContracts;
}
