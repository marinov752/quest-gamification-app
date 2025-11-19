package com.questgamification.service;

import com.questgamification.domain.dto.UserRegistrationDto;
import com.questgamification.domain.entity.Role;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        logger.info("Attempting to register user with username: {}", registrationDto.getUsername());
        
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setLevel(1);
        user.setExperiencePoints(0L);
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.USER);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#user.id")
    public User addExperience(User user, Long experiencePoints) {
        logger.info("Adding {} XP to user {}", experiencePoints, user.getUsername());
        user.setExperiencePoints(user.getExperiencePoints() + experiencePoints);
        
        int newLevel = calculateLevel(user.getExperiencePoints());
        if (newLevel > user.getLevel()) {
            logger.info("User {} leveled up from {} to {}", user.getUsername(), user.getLevel(), newLevel);
            user.setLevel(newLevel);
        }
        
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    private int calculateLevel(Long experiencePoints) {
        return (int) Math.floor(Math.sqrt(experiencePoints / 100.0)) + 1;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User updateUserRole(UUID userId, Role role) {
        logger.info("Updating role for user {} to {}", userId, role);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getRoles().clear();
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}

