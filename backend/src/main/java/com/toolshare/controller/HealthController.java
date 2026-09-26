package com.toolshare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Health check controller for uptime monitoring and Vercel/Render connection testing.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "ToolShare Spring Boot Backend");
        status.put("version", "1.0.0");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            res.put("status", "CONNECTED");
            res.put("database", jdbcTemplate.queryForObject("SELECT DATABASE()", String.class));
            List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);
            res.put("tables", tables);
            if (tables.contains("tools") || tables.contains("TOOLS")) {
                res.put("tools_count", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tools", Long.class));
            }
            if (tables.contains("users") || tables.contains("USERS")) {
                res.put("users_count", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class));
            }
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("error", e.getMessage());
            if (e.getCause() != null) {
                res.put("cause", e.getCause().getMessage());
            }
        }
        return ResponseEntity.ok(res);
    }
}
