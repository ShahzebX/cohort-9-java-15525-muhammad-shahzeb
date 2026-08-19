package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.UserRepository;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user){
        boolean noPhone = user.getPhone() == null || user.getPhone().isEmpty();
        boolean noEmail = user.getEmail() == null || user.getEmail().isEmpty();

        if (noPhone && noEmail)
            throw new IllegalArgumentException("User must register using either Email or Phone");

        if (!noPhone && userRepository.findByPhone(user.getPhone()).isPresent())
            throw new DuplicateResourceException("Phone already in use: " + user.getPhone());

        if (!noEmail && userRepository.findByEmail(user.getEmail()).isPresent())
            throw new DuplicateResourceException("Email already in use: " + user.getEmail());

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        logger.info("Registering new user with identifier: {}", noEmail ? user.getPhone() : user.getEmail());

        return userRepository.save(user);
    }

    public User findByEmailOrPhone(String identifier){
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("No user found with: " + identifier));
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword){
        User user = userRepository.findById(userId).orElseThrow(() -> {
            logger.warn("Password change failed — user not found with id: {}", userId);
            return new ResourceNotFoundException("User not found");
        });
        boolean passwordMatch = passwordEncoder.matches(oldPassword, user.getPasswordHash());

        if (!passwordMatch)
            throw new IllegalArgumentException("Old password is incorrect");

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
