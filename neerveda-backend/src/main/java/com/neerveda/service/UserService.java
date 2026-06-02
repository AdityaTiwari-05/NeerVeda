package com.neerveda.service;

import com.neerveda.exception.NeerVedaException;
import com.neerveda.model.User;
import com.neerveda.repository.FirestoreRepository;
import com.neerveda.security.NeerVedaUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 👤 UserService
 *
 * Manages user CRUD and implements Spring Security UserDetailsService.
 * Uses Firestore "users" collection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final String COLLECTION = "users";

    private final FirestoreRepository firestoreRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------
    // Spring Security — UserDetailsService
    // -------------------------------------------------------

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new NeerVedaUserPrincipal(user);
    }

    // -------------------------------------------------------
    // FIND
    // -------------------------------------------------------

    public Optional<User> findByEmail(String email) {
        List<Map<String, Object>> results =
            firestoreRepository.findByField(COLLECTION, "email", email);
        if (results.isEmpty()) return Optional.empty();
        return Optional.of(mapToUser(results.get(0)));
    }

    public Optional<User> findById(String id) {
        return firestoreRepository.findById(COLLECTION, id)
            .map(this::mapToUser);
    }

    public List<User> findAll() {
        return firestoreRepository.findAll(COLLECTION).stream()
            .map(this::mapToUser)
            .toList();
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    public User createUser(User user) {
        // Check duplicate email
        if (findByEmail(user.getEmail()).isPresent()) {
            throw NeerVedaException.conflict("Email already registered: " + user.getEmail());
        }

        String id = UUID.randomUUID().toString();
        user.setId(id);
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        firestoreRepository.save(COLLECTION, id, userToMap(user));
        log.info("✅ Created user: {} ({})", user.getEmail(), user.getRole());
        return user;
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    public User updateUser(String id, Map<String, Object> updates) {
        updates.remove("passwordHash"); // Never allow direct hash update
        updates.remove("id");
        firestoreRepository.update(COLLECTION, id, updates);
        return findById(id)
            .orElseThrow(() -> NeerVedaException.notFound("User", id));
    }

    public void updateRefreshToken(String userId, String refreshToken) {
        Map<String, Object> update = Map.of("refreshToken", refreshToken);
        firestoreRepository.update(COLLECTION, userId, update);
    }

    public void updateLastLogin(String userId) {
        Map<String, Object> update = Map.of("lastLogin", LocalDateTime.now().toString());
        firestoreRepository.update(COLLECTION, userId, update);
    }

    public void deactivateUser(String id) {
        firestoreRepository.update(COLLECTION, id, Map.of("active", false));
        log.info("🔒 Deactivated user: {}", id);
    }

    // -------------------------------------------------------
    // MAPPING
    // -------------------------------------------------------

    private User mapToUser(Map<String, Object> data) {
        return User.builder()
            .id(str(data, "id"))
            .name(str(data, "name"))
            .email(str(data, "email"))
            .passwordHash(str(data, "passwordHash"))
            .role(data.get("role") != null
                ? User.Role.valueOf(str(data, "role")) : User.Role.PUBLIC_VIEWER)
            .active(data.get("active") instanceof Boolean b ? b : true)
            .phone(str(data, "phone"))
            .district(str(data, "district"))
            .state(str(data, "state"))
            .build();
    }

    private Map<String, Object> userToMap(User u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("email", u.getEmail());
        map.put("passwordHash", u.getPasswordHash());
        map.put("role", u.getRole().name());
        map.put("active", u.isActive());
        map.put("phone", u.getPhone());
        map.put("district", u.getDistrict());
        map.put("state", u.getState());
        map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
        return map;
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
