package com.studentmarket.marketplace.repository;

import com.studentmarket.marketplace.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}