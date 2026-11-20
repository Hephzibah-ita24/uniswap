package com.studentmarket.marketplace.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateListingRequest(
        @NotBlank @Size(max = 150) String title,
        String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        Long categoryId,
        @NotBlank @Pattern(regexp = "USED|GOOD|NEW") String itemCondition,
        @Pattern(regexp = "AVAILABLE|SOLD") String status) {}