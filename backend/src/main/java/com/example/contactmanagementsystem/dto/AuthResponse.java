package com.example.contactmanagementsystem.dto;

public class AuthResponse {
    private String token;
    private String identifier;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public AuthResponse(String token, String identifier) {
        this.token = token;
        this.identifier = identifier;
    }

    public AuthResponse(String token, String identifier, String firstName, String lastName, String email, String phone) {
        this.token = token;
        this.identifier = identifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}