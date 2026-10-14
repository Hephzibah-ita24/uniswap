package com.studentmarket.marketplace.controller;

import com.studentmarket.marketplace.dto.ListingResponse;
import com.studentmarket.marketplace.service.ListingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
            @RequestParam(name = "sort", required = false) String sort) {
        return listingService.search(facultyId, categoryId, condition, minPrice, maxPrice, sort);
    }

    @GetMapping("/{id}")
    public ListingResponse getById(@PathVariable("id") Long id) {
        return listingService.getById(id);
    }
}
