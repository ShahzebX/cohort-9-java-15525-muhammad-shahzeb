package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.services.UserService;
import com.example.contactmanagementsystem.dto.AuthResponse;
import com.example.contactmanagementsystem.dto.LoginRequest;
import com.example.contactmanagementsystem.dto.RegisterRequest;
import com.example.contactmanagementsystem.security.JwtUtil;
import com.example.exception.InvalidCredentialsException;
import com.example.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
        String token = jwtUtil.generateToken(identifier);

        return new AuthResponse(token, identifier);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        User user;
        try {
            user = userService.findByEmailOrPhone(loginRequest.getIdentifier());
        } catch (ResourceNotFoundException e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash()))
            throw new InvalidCredentialsException("Invalid credentials");

        String token = jwtUtil.generateToken(loginRequest.getIdentifier());

        return new AuthResponse(token, loginRequest.getIdentifier());
    }
}