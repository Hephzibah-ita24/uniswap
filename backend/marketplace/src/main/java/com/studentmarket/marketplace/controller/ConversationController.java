package com.studentmarket.marketplace.controller;

import com.studentmarket.marketplace.dto.*;
import com.studentmarket.marketplace.entity.Conversation;
import com.studentmarket.marketplace.entity.Listing;
import com.studentmarket.marketplace.entity.Message;
import com.studentmarket.marketplace.entity.User;
import com.studentmarket.marketplace.repository.ConversationRepository;
import com.studentmarket.marketplace.repository.ListingRepository;
import com.studentmarket.marketplace.repository.MessageRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;

    public ConversationController(ConversationRepository conversationRepository,
                                  MessageRepository messageRepository,
                                  ListingRepository listingRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.listingRepository = listingRepository;
    }

    // 1. Buyer starts a conversation (or gets the existing one)
    @PostMapping
    public ConversationResponse start(@AuthenticationPrincipal User me,
                                      @Valid @RequestBody StartConversationRequest req) {
        Listing listing = listingRepository.findById(req.listingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        if (listing.getSeller().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't message your own listing");
        }

        Conversation conversation = conversationRepository
                .findByListingIdAndBuyerId(listing.getId(), me.getId())
                .orElseGet(() -> {
                    try {
                        Conversation c = new Conversation();
                        c.setListing(listing);
                        c.setBuyer(me);
                        c.setSeller(listing.getSeller());
                        return conversationRepository.save(c);
                    } catch (DataIntegrityViolationException e) {
                        // unique (listing_id, buyer_id) hit: someone created it first, so return that one
                        return conversationRepository
                                .findByListingIdAndBuyerId(listing.getId(), me.getId())
                                .orElseThrow(() -> e);
                    }
                });

        return toResponse(conversation);
    }

    // 2. Inbox: conversations where I'm the buyer or the seller
    @GetMapping
    public List<ConversationResponse> inbox(@AuthenticationPrincipal User me) {
        return conversationRepository
                .findByBuyerIdOrSellerIdOrderByCreatedAtDesc(me.getId(), me.getId())
                .stream().map(this::toResponse).toList();
    }

    // 3a. Send a message
    @PostMapping("/{id}/messages")
    public MessageResponse send(@AuthenticationPrincipal User me,
                                @PathVariable Long id,
                                @Valid @RequestBody SendMessageRequest req) {
        Conversation conversation = getForParticipant(id, me);
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(me);
        message.setContent(req.content().trim());
        return toResponse(messageRepository.save(message));
    }

    // 3b. Read all messages (the frontend polls this)
    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@AuthenticationPrincipal User me,
                                          @PathVariable Long id) {
        getForParticipant(id, me);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(id)
                .stream().map(this::toResponse).toList();
    }

    // Only the buyer or seller of a conversation may touch it
    private Conversation getForParticipant(Long id, User me) {
        Conversation c = conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        boolean participant = c.getBuyer().getId().equals(me.getId())
                || c.getSeller().getId().equals(me.getId());
        if (!participant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your conversation");
        }
        return c;
    }

    private ConversationResponse toResponse(Conversation c) {
        return new ConversationResponse(c.getId(),
                c.getListing().getId(), c.getListing().getTitle(),
                c.getBuyer().getId(), c.getBuyer().getFullName(),
                c.getSeller().getId(), c.getSeller().getFullName(),
                c.getCreatedAt());
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(m.getId(), m.getSender().getId(), m.getSender().getFullName(),
                m.getContent(), m.isRead(), m.getCreatedAt());
    }
}