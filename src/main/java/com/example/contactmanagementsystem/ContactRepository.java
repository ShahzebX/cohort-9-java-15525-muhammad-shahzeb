package com.example.contactmanagementsystem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByUser(User user);
    Page<Contact> findByUser(User user, Pageable pageable);
    Optional<Contact> findByIdAndUser(Integer id, User user);
    List<Contact> findByUserAndFirstNameContainingIgnoreCaseOrUserAndLastNameContainingIgnoreCase(User user, String firstName, User user2, String lastName);
}
