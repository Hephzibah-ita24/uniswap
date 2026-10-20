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
import com.studentmarket.marketplace.dto.CreateListingRequest;
import com.studentmarket.marketplace.repository.CategoryRepository;
import com.studentmarket.marketplace.repository.FacultyRepository;
import com.studentmarket.marketplace.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final CategoryRepository categoryRepository;

    public ListingService(ListingRepository listingRepository, UserRepository userRepository,
                          FacultyRepository facultyRepository, CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.categoryRepository = categoryRepository;
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
    @Transactional
    public ListingResponse create(String email, CreateListingRequest req) {
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Faculty faculty = facultyRepository.findById(req.facultyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid faculty ID"));
        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID"));
        }

        Listing l = new Listing();
        l.setSeller(seller);
        l.setFaculty(faculty);
        l.setCategory(category);
        l.setTitle(req.title());
        l.setDescription(req.description());
        l.setPrice(req.price());
        l.setItemCondition(req.itemCondition());
        l.setStatus("AVAILABLE");

        List<String> urls = req.imageUrls() == null ? List.of() : req.imageUrls();
        for (int i = 0; i < urls.size(); i++) {
            ItemImage img = new ItemImage();
            img.setListing(l);
            img.setImageUrl(urls.get(i));
            img.setPrimary(i == 0);
            l.getImages().add(img);
        }
        return toResponse(listingRepository.save(l));
    }

    @Transactional
    public void delete(String email, Long id) {
        Listing l = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        if (!l.getSeller().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your listing");
        }
        listingRepository.delete(l);
    }
}
