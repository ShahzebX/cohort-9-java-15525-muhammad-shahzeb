package com.example.contactmanagementsystem;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

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
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "contact")
    private List<Email> emails;

    @OneToMany(mappedBy = "contact")
    private List<Phone> phones;



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

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
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

    public List<Phone> getPhones() {
        return phones;
    }

    public void setId(Integer id){
        this.id = id;
    }

    public void setUser(User user){
        this.user = user;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }
}

