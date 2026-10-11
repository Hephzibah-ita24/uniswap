package com.studentmarket.marketplace.controller;

import com.studentmarket.marketplace.dto.*;
import com.studentmarket.marketplace.entity.User;
import com.studentmarket.marketplace.repository.UserRepository;
import com.studentmarket.marketplace.security.JwtService;
import com.studentmarket.marketplace.repository.FacultyRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FacultyRepository facultyRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          FacultyRepository facultyRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.facultyRepository = facultyRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setStudentId(req.studentId());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName());
        if (req.facultyId() != null) {
            var faculty = facultyRepository.findById(req.facultyId());
            if (faculty.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid faculty ID"));
            }
            user.setFaculty(faculty.get());
        }
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email or student ID already registered"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return userRepository.findByEmail(req.email())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(
                        Map.of("token", jwtService.generateToken(u.getId(), u.getEmail()))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password")));
    }
}
