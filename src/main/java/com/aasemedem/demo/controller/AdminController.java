package com.aasemedem.demo.controller;

import com.aasemedem.demo.dto.response.AuthResponse;
import com.aasemedem.demo.entity.Role;
import com.aasemedem.demo.entity.User;
import com.aasemedem.demo.repository.UserRepository;
import com.aasemedem.demo.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-only user management.
 * Admins can create SELLER or ADMIN accounts and manage all users.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** GET /api/admin/users */
    @GetMapping
    public Page<Map<String, Object>> list(@PageableDefault(size = 20) Pageable pageable) {
        return userRepo.findAll(pageable).map(u -> Map.of(
                "id",        u.getId(),
                "name",      u.getName(),
                "email",     u.getEmail(),
                "role",      u.getRole().name(),
                "isActive",  u.getIsActive(),
                "createdAt", u.getCreatedAt().toString()
        ));
    }

    /** POST /api/admin/users — create any role user */
    @PostMapping
    public ResponseEntity<AuthResponse> create(@Valid @RequestBody CreateUserReq req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + req.getEmail());
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.valueOf(req.getRole()))
                .build();
        userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build());
    }

    /** PATCH /api/admin/users/{id}/role */
    @PatchMapping("/{id}/role")
    public Map<String, String> changeRole(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setRole(Role.valueOf(body.get("role")));
        userRepo.save(user);
        return Map.of("role", user.getRole().name());
    }

    /** PATCH /api/admin/users/{id}/toggle-active */
    @PatchMapping("/{id}/toggle-active")
    public Map<String, Boolean> toggleActive(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setIsActive(!user.getIsActive());
        userRepo.save(user);
        return Map.of("isActive", user.getIsActive());
    }

    // ── Inner DTO ─────────────────────────────────────────────

    @Data
    public static class CreateUserReq {
        @NotBlank private String name;
        @Email @NotBlank private String email;
        @NotBlank private String password;
        @NotBlank private String role;   // ADMIN | SELLER | BUYER
    }
}

