package dev.hltech.dredd.domain.environment;

public interface Service {

    String getName();

    String getVersion();

    Provider asProvider();

    Consumer asConsumer();

}
