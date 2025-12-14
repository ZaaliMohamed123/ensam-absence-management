package com.ensam.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/auth/**")
                        .uri("lb://USER-SERVICE"))

                // Academic Service Routes
                .route("academic-service", r -> r
                        .path("/api/classes/**", "/api/modules/**", "/api/sessions/**")
                        .uri("lb://ACADEMIC-SERVICE"))

                // Absence Service Routes
                .route("absence-service", r -> r
                        .path("/api/absences/**")
                        .uri("lb://ABSENCE-SERVICE"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://NOTIFICATION-SERVICE"))

                // Justification Service Routes
                .route("justification-service", r -> r
                        .path("/api/justifications/**")
                        .uri("lb://JUSTIFICATION-SERVICE"))

                // Reporting Service Routes
                .route("reporting-service", r -> r
                        .path("/api/reports/**")
                        .uri("lb://REPORTING-SERVICE"))

                .build();
    }
}
