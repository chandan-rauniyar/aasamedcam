package com.aasemedem.demo.controller;

import com.aasemedem.demo.dto.request.AuthRequest;
import com.aasemedem.demo.dto.response.AuthResponse;
import com.aasemedem.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Body: { "email": "...", "password": "..." }
     * Returns JWT + role
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.Login req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * POST /api/auth/register
     * Self-registration — always creates BUYER role.
     * Admins use /api/admin/users to create SELLER/ADMIN accounts.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest.Register req) {
        return ResponseEntity.ok(authService.register(req));
    }
}

