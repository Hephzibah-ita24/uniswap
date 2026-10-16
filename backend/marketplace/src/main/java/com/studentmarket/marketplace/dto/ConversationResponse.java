package com.studentmarket.marketplace.dto;

import java.time.LocalDateTime;

public record ConversationResponse(Long id, Long listingId, String listingTitle,
                                   Long buyerId, String buyerName,
                                   Long sellerId, String sellerName,
                                   LocalDateTime createdAt) {}
