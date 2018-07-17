package dev.hltech.dredd.domain.environment;

import au.com.dius.pact.model.RequestResponsePact;

import java.util.Optional;

public interface Consumer {

    Optional<RequestResponsePact> getPact(String providerName);
}
