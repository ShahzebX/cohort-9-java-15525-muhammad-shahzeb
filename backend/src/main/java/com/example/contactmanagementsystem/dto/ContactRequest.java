package com.example.contactmanagementsystem.dto;

import jakarta.validation.Valid;
import java.util.List;

public class ContactRequest {
    private String firstName;
    private String lastName;
    private String title;
    @Valid
    private List<EmailRequest> emails;
    @Valid
    private List<PhoneRequest> phones;

    public ContactRequest() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<EmailRequest> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailRequest> emails) {
        this.emails = emails;
    }

    public List<PhoneRequest> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneRequest> phones) {
        this.phones = phones;
    }
}