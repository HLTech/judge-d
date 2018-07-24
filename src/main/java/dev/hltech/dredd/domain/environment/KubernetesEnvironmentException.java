package dev.hltech.dredd.domain.environment;

public class KubernetesEnvironmentException extends RuntimeException {

    public KubernetesEnvironmentException(String message) {
        super(message);
    }

    public KubernetesEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
