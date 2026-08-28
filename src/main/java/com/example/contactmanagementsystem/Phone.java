package com.example.contactmanagementsystem;

import jakarta.persistence.*;

@Entity
@Table(name = "phone")
public class Phone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;

    public Phone() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
