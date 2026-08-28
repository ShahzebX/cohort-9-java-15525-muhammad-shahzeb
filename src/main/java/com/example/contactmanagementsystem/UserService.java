package com.example.contactmanagementsystem;

import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user){
        if (user == null)
            throw new IllegalArgumentException("User must not be null");

        boolean noPhone = user.getPhone() == null || user.getPhone().isBlank();
        boolean noEmail = user.getEmail() == null || user.getEmail().isBlank();

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank())
            throw new IllegalArgumentException("Password must not be blank");

        if (noPhone && noEmail)
            throw new IllegalArgumentException("User must register using either Email or Phone");

        if (!noPhone && userRepository.findByPhone(user.getPhone()).isPresent())
            throw new DuplicateResourceException("Phone already in use: " + user.getPhone());

        if (!noEmail && userRepository.findByEmail(user.getEmail()).isPresent())
            throw new DuplicateResourceException("Email already in use: " + user.getEmail());

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        try {
            return userRepository.save(user);
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

        if(newPassword.equals(oldPassword))
            throw new IllegalArgumentException("New password cannot be the same as the old password");

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean passwordMatch = passwordEncoder.matches(oldPassword, user.getPasswordHash());

        if (!passwordMatch)
            throw new IllegalArgumentException("Old password is incorrect");

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Failed to update password.", e);
        }
    }
}
