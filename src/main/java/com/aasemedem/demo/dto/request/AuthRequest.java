package com.aasemedem.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthRequest {

    @Data
    public static class Login {
        @Email @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class Register {
        @NotBlank
        private String name;
        @Email @NotBlank
        private String email;
        @NotBlank
        private String password;
        // role defaults to BUYER; ADMIN/SELLER registered by admin
    }
}

