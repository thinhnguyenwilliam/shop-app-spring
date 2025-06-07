package com.example.shopapp.configuration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

// {{API_PREFIX_3}}shopapp/api/v1/actuator/health
/// actuator/info – Application info (add in properties)
///actuator/metrics – Application metrics (e.g., memory, CPU)
///actuator/env – Environment properties
///actuator/beans – List of all Spring beans

@Component
public class CustomHealthCheck implements HealthIndicator {

    @Override
    public Health health() {
        try {
            String computerName = InetAddress.getLocalHost().getHostName();
            return Health.up()
                    .withDetail("computerName", computerName)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
