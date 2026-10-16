package com.studentmarket.marketplace.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateListingRequest(
        @NotBlank @Size(max = 150) String title,
        String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull Long facultyId,
        Long categoryId,
        @NotBlank @Pattern(regexp = "USED|GOOD|NEW") String itemCondition,
        @Size(max = 5) List<@NotBlank @Pattern(regexp = "^https://.+") String> imageUrls) {}