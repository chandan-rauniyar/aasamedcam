package com.aasemedem.demo.Test;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Hit GET /api/health/db to confirm Neon connection is alive.
 * Remove or secure this endpoint before production.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class DBTest {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> checkDb() {
        try {
            // Simple query to ping Neon
            String dbTime = jdbcTemplate.queryForObject(
                    "SELECT NOW()::TEXT", String.class
            );

            // Also check tables exist
            Integer userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users", Integer.class
            );

            return ResponseEntity.ok(Map.of(
                    "status",     "OK",
                    "db_time",    dbTime,
                    "user_count", userCount,
                    "server_time", LocalDateTime.now().toString()
            ));

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "status",  "FAILED",
                    "error",   ex.getMessage(),
                    "hint",    "Check application.properties — datasource URL, username, password"
            ));
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "time",   LocalDateTime.now().toString()
        ));
    }
}
