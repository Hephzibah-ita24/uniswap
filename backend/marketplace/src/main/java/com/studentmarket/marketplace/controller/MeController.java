package com.studentmarket.marketplace.controller;

import com.studentmarket.marketplace.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, String> me(@AuthenticationPrincipal User user) {
        return Map.of("email", user.getEmail());
    }
}
