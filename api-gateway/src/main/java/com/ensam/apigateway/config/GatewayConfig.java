package com.ensam.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
                .GET("/api/users/**", http())
                .filter(lb("user-service"))
                .POST("/api/auth/**", http())
                .filter(lb("user-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> academicServiceRoute() {
        return route("academic-service")
                .GET("/api/classes/**", http())
                .filter(lb("academic-service"))
                .GET("/api/modules/**", http())
                .filter(lb("academic-service"))
                .GET("/api/sessions/**", http())
                .filter(lb("academic-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> absenceServiceRoute() {
        return route("absence-service")
                .route(path("/api/absences/**"), http())
                .filter(lb("absence-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return route("notification-service")
                .route(path("/api/notifications/**"), http())
                .filter(lb("notification-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> justificationServiceRoute() {
        return route("justification-service")
                .route(path("/api/justifications/**"), http())
                .filter(lb("justification-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> reportingServiceRoute() {
        return route("reporting-service")
                .route(path("/api/reports/**"), http())
                .filter(lb("reporting-service"))
                .build();
    }
}
