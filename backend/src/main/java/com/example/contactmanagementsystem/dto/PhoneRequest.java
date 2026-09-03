package com.example.contactmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PhoneRequest {
    @NotBlank(message = "Phone number must not be blank")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;
    private String label;

    public PhoneRequest() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}