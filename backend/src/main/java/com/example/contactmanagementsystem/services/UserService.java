package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.UserRepository;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Minimum number of characters required in a password. Must match the frontend policy. */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validates that the supplied plain-text password meets the shared policy:
     * at least {@value #MIN_PASSWORD_LENGTH} characters, at least one letter,
     * and at least one digit.  Throws {@link IllegalArgumentException} (mapped to
     * HTTP 400 by {@code GlobalExceptionHandler}) when the policy is violated.
     */
    private void enforcePasswordPolicy(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH
                || !password.matches(".*[a-zA-Z].*")
                || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH
                            + " characters and contain both letters and digits.");
        }
    }

    public User registerUser(User user){
        if (user == null)
            throw new IllegalArgumentException("User must not be null");

        boolean noPhone = user.getPhone() == null || user.getPhone().isBlank();
        boolean noEmail = user.getEmail() == null || user.getEmail().isBlank();

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank())
            throw new IllegalArgumentException("Password must not be blank");

        enforcePasswordPolicy(user.getPasswordHash());

        if (noPhone && noEmail)
            throw new IllegalArgumentException("User must register using either Email or Phone");

        if (!noPhone && userRepository.findByPhone(user.getPhone()).isPresent())
            throw new DuplicateResourceException("Phone already in use: " + user.getPhone());

        if (!noEmail && userRepository.findByEmail(user.getEmail()).isPresent())
            throw new DuplicateResourceException("Email already in use: " + user.getEmail());

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        try {
            User saved = userRepository.save(user);
            logger.info("Registered new user id={}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    throw new DuplicateResourceException("Email or phone already in use.");
                }
                cause = cause.getCause();
            }
            throw e;
        }
    }

    public User findByEmailOrPhone(String identifier){
        if (identifier == null || identifier.isBlank())
            throw new IllegalArgumentException("Identifier must not be blank");

        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("No user found with: " + identifier));
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword){
        if (userId == null)
            throw new IllegalArgumentException("User ID must not be null");
        if (oldPassword == null || oldPassword.isBlank())
            throw new IllegalArgumentException("Old password cannot be blank");
        if (newPassword == null || newPassword.isBlank())
            throw new IllegalArgumentException("New password cannot be blank");

        enforcePasswordPolicy(newPassword);

        if(newPassword.equals(oldPassword))
            throw new IllegalArgumentException("New password cannot be the same as the old password");

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean passwordMatch = passwordEncoder.matches(oldPassword, user.getPasswordHash());

        if (!passwordMatch)
            throw new IllegalArgumentException("Old password is incorrect");

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        try {
            userRepository.save(user);
            logger.info("Password changed for user id={}", userId);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Failed to update password.", e);
        }
    }
}
