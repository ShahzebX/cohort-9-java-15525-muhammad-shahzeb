package com.example.contactmanagementsystem.dto;

public class AuthResponse {
    private String token;
    private String identifier;

    public AuthResponse(String token, String identifier) {
        this.token = token;
        this.identifier = identifier;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}