package kln.ams.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "AMS Community Service API");
        response.put("status", "ONLINE");
        response.put("version", "0.0.1-SNAPSHOT");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("health", "/actuator/health");
        endpoints.put("info", "/actuator/info");
        endpoints.put("swagger", "/swagger-ui.html");
        endpoints.put("openapi", "/v3/api-docs");
        endpoints.put("visitors", "/api/v1/visitors");
        endpoints.put("announcements", "/api/v1/announcements");
        endpoints.put("notifications", "/api/v1/notifications");
        endpoints.put("internalNotifications", "/api/v1/internal/notifications");

        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }
}
