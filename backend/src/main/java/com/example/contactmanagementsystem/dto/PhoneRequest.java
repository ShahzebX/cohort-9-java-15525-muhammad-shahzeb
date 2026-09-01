package com.example.contactmanagementsystem.dto;

public class PhoneRequest {
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