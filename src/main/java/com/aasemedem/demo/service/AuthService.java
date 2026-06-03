package com.aasemedem.demo.service;

import com.aasemedem.demo.dto.request.AuthRequest;
import com.aasemedem.demo.dto.response.AuthResponse;
import com.aasemedem.demo.entity.Role;
import com.aasemedem.demo.entity.User;
import com.aasemedem.demo.repository.UserRepository;
import com.aasemedem.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    // ── Login ─────────────────────────────────────────────────
    public AuthResponse login(AuthRequest.Login req) {
        // Throws BadCredentialsException if wrong
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user, user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    // ── Register (self-registration = BUYER only) ─────────────
    public AuthResponse register(AuthRequest.Register req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.BUYER)      // self-register is always BUYER
                .build();

        userRepo.save(user);
        String token = jwtUtil.generateToken(user, user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}

