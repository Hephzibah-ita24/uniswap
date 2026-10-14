package com.studentmarket.marketplace.service;

import com.studentmarket.marketplace.dto.ListingResponse;
import com.studentmarket.marketplace.entity.*;
import com.studentmarket.marketplace.repository.ListingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> search(Long facultyId, Long categoryId, String condition,
                                        BigDecimal minPrice, BigDecimal maxPrice, String sort) {

        Specification<Listing> spec = (root, query, cb) -> cb.equal(root.get("status"), "AVAILABLE");

        if (facultyId != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("faculty").get("id"), facultyId));
        }
        if (categoryId != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("category").get("id"), categoryId));
        }
        if (condition != null && !condition.isBlank()) {
            String c = condition.toUpperCase();
            spec = spec.and((r, q, cb) -> cb.equal(r.get("itemCondition"), c));
        }
        if (minPrice != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.<BigDecimal>get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.<BigDecimal>get("price"), maxPrice));
        }

        Sort order = switch (sort == null ? "recent" : sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };

        return listingRepository.findAll(spec, order).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ListingResponse getById(Long id) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        return toResponse(l);
    }

    private ListingResponse toResponse(Listing l) {
        List<String> urls = l.getImages().stream().map(ItemImage::getImageUrl).toList();
        String primary = l.getImages().stream()
                .filter(i -> Boolean.TRUE.equals(i.getPrimary()))
                .map(ItemImage::getImageUrl)
                .findFirst()
                .orElse(urls.isEmpty() ? null : urls.get(0));
        Category c = l.getCategory();
        return new ListingResponse(
                l.getId(), l.getTitle(), l.getDescription(), l.getPrice(),
                l.getStatus(), l.getItemCondition(),
                l.getFaculty().getId(), l.getFaculty().getName(),
                c == null ? null : c.getId(), c == null ? null : c.getName(),
                l.getSeller().getId(), l.getSeller().getFullName(),
                primary, urls, l.getCreatedAt());
    }
}
