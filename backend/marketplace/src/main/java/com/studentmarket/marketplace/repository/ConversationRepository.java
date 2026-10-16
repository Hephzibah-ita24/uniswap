package com.studentmarket.marketplace.repository;

import com.studentmarket.marketplace.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByListingIdAndBuyerId(Long listingId, Long buyerId);
    List<Conversation> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}
