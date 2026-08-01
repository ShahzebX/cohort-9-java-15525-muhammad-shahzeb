package com.example.contactmanagementsystem;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "phone")
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;

    private Integer phoneNumber;
    private String label;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Phone(){
    }

    public Phone(
            Integer id,
            Contact contact,
            Integer phoneNumber,
            String label,
            Timestamp createdAt,
            Timestamp updatedAt) {

        this.id = id;
        this.contact = contact;
        this.phoneNumber = phoneNumber;
        this.label = label;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}