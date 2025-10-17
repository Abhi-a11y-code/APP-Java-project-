package com.example.chat.controller;

import com.example.chat.dto.LoginRequest;
import com.example.chat.model.User;
import com.example.chat.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        if (req.username == null || req.password == null) return ResponseEntity.badRequest().body("Missing fields");
        if (userRepository.findByUsername(req.username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        String hash = sha256(req.password);
        User u = new User(req.username, hash);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> u = userRepository.findByUsername(req.username);
        if (u.isPresent() && u.get().getPasswordHash().equals(sha256(req.password))) {
            return ResponseEntity.ok(Map.of("id", u.get().getId(), "username", u.get().getUsername()));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
