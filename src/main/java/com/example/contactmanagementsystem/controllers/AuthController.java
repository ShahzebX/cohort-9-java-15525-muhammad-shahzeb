package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.services.UserService;
import com.example.contactmanagementsystem.dto.AuthResponse;
import com.example.contactmanagementsystem.dto.LoginRequest;
import com.example.contactmanagementsystem.security.JwtUtil;
import com.example.exception.InvalidCredentialsException;
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
    public AuthResponse register(@RequestBody User user) {
        userService.registerUser(user);

        String identifier = (user.getEmail() != null) ? user.getEmail() : user.getPhone();
        String token = jwtUtil.generateToken(identifier);

        return new AuthResponse(token, identifier);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        userService.findByEmailOrPhone(loginRequest.getIdentifier());

        boolean match = passwordEncoder.matches(loginRequest.getPassword(), userService.findByEmailOrPhone(loginRequest.getIdentifier()).getPasswordHash());

        if(!match)
            throw new InvalidCredentialsException("Invalid credentials for identifier: " + loginRequest.getIdentifier());

        String token = jwtUtil.generateToken(loginRequest.getIdentifier());

        return new AuthResponse(token, loginRequest.getIdentifier());
    }
}