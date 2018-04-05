package dev.hltech.dredd.management.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
            return Health
                .down()
                .withDetail("Error Code", errorCode)
                .build();
        }
        return Health
            .up()
            .withDetail("foo", "bar")
            .build();
    }

    private int check() {
        // perform some specific health check
        return 0;
    }
}
