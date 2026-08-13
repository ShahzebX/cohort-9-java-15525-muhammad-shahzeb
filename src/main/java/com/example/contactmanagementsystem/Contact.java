package com.example.contactmanagementsystem;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String firstName;
    private String lastName;
    private String title;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Email> emails = new ArrayList<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phone> phones = new ArrayList<>();



    public Contact(){
    }

    public Contact(
            Integer id,
            User user,
            String firstName,
            String lastName,
            String title,
            Timestamp createdAt,
            Timestamp updatedAt){

        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addEmail(Email email){
        Objects.requireNonNull(email, "email must not be null");

        if (email.getContact() != null)
            throw new IllegalStateException("Email is already assigned to a contact");

        emails.add(email);
        email.setContact(this);
    }

    public void removeEmail(Email email){
        emails.remove(email);
        email.setContact(null);
    }

    public void addPhone(Phone phone){
        Objects.requireNonNull(phone, "phone number must not be null");

        if(phone.getContact() != null)
            throw new IllegalStateException("Phone is already assigned to a contact");


        phones.add(phone);
        phone.setContact(this);
    }

    public void removePhone(Phone phone){
        phones.remove(phone);
        phone.setContact(null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }
}

