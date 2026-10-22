package com.studentmarket.marketplace.dto;

import java.time.LocalDateTime;

public record MessageResponse(Long id, Long senderId, String senderName,
                              String content, boolean read, LocalDateTime createdAt) {}