package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.services.UserService;
import com.example.contactmanagementsystem.dto.AuthResponse;
import com.example.contactmanagementsystem.dto.ChangePasswordRequest;
import com.example.contactmanagementsystem.dto.LoginRequest;
import com.example.contactmanagementsystem.dto.RegisterRequest;
import com.example.contactmanagementsystem.security.JwtUtil;
import com.example.exception.InvalidCredentialsException;
import com.example.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(request.getPassword());

        userService.registerUser(user);

        String identifier = (user.getEmail() != null && !user.getEmail().isBlank()) ? user.getEmail() : user.getPhone();
        logger.info("Registration successful");
        String token = jwtUtil.generateToken(identifier);

        return new AuthResponse(token, identifier, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        User user;
        try {
            user = userService.findByEmailOrPhone(loginRequest.getIdentifier());
        } catch (ResourceNotFoundException e) {
            logger.warn("Login failed: unknown identifier");
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            logger.warn("Login failed: incorrect password");
            throw new InvalidCredentialsException("Invalid credentials");
        }

        logger.info("Login successful");
        String token = jwtUtil.generateToken(loginRequest.getIdentifier());

        return new AuthResponse(token, loginRequest.getIdentifier(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone());
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        String identifier = authentication.getName();
        User user = userService.findByEmailOrPhone(identifier);
        userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        logger.info("Password changed successfully");
    }
}