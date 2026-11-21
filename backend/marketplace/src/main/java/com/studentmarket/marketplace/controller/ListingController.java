package com.studentmarket.marketplace.controller;
import com.studentmarket.marketplace.dto.CreateListingRequest;
import com.studentmarket.marketplace.dto.UpdateListingRequest; // NEW
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.studentmarket.marketplace.dto.ListingResponse;
import com.studentmarket.marketplace.service.ListingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import com.studentmarket.marketplace.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public List<ListingResponse> search(
            @RequestParam(name = "facultyId", required = false) Long facultyId,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "condition", required = false) String condition,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "q", required = false) String q) {
        return listingService.search(facultyId, categoryId, condition, minPrice, maxPrice, sort, q);
    }

    // NEW: my own listings (login required)
    @GetMapping("/mine")
    public List<ListingResponse> mine(@AuthenticationPrincipal User user) {
        return listingService.mine(user.getEmail());
    }

    @GetMapping("/{id}/similar")
    public List<ListingResponse> similar(@PathVariable("id") Long id) {
        return listingService.similar(id);
    }

    @GetMapping("/{id}")
    public ListingResponse getById(@PathVariable("id") Long id) {
        return listingService.getById(id);
    }
    @PostMapping
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody CreateListingRequest req,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingService.create(user.getEmail(), req));
    }

    // NEW: edit a listing (seller only)
    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable("id") Long id,
                                  @Valid @RequestBody UpdateListingRequest req,
                                  @AuthenticationPrincipal User user) {
        return listingService.update(user.getEmail(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id,
                                       @AuthenticationPrincipal User user) {
        listingService.delete(user.getEmail(), id);
        return ResponseEntity.noContent().build();
    }
}