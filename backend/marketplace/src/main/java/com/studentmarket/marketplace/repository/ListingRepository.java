package com.studentmarket.marketplace.repository;

import com.studentmarket.marketplace.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ListingRepository
        extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    List<Listing> findBySellerEmailOrderByIdDesc(String email); // NEW
}
