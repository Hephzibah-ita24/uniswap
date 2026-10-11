package com.studentmarket.marketplace.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String studentId,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String fullName,
        Long facultyId) {}
