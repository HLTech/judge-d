package dev.hltech.dredd.management.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health
            .up()
            .withDetail("foo", "bar")
            .build();
    }

}
