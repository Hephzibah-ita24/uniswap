package com.studentmarket.marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ListingResponse(
        Long id, String title, String description, BigDecimal price,
        String status, String itemCondition,
        Long facultyId, String facultyName,
        Long categoryId, String categoryName,
        Long sellerId, String sellerName,
        String primaryImageUrl, List<String> imageUrls,
        LocalDateTime createdAt) {}
