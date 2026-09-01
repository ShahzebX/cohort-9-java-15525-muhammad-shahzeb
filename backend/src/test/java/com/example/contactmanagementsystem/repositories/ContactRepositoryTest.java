package com.example.contactmanagementsystem.repositories;

import com.example.contactmanagementsystem.Contact;
import com.example.contactmanagementsystem.ContactRepository;
import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPhone("1111111111");
        user1.setPasswordHash("hash1");
        userRepository.save(user1);

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPhone("2222222222");
        user2.setPasswordHash("hash2");
        userRepository.save(user2);

        Contact c1 = new Contact();
        c1.setFirstName("Alice");
        c1.setLastName("Smith");
        c1.setUser(user1);
        contactRepository.save(c1);

        Contact c2 = new Contact();
        c2.setFirstName("Bob");
        c2.setLastName("Jones");
        c2.setUser(user1);
        contactRepository.save(c2);

        Contact c3 = new Contact();
        c3.setFirstName("Charlie");
        c3.setLastName("Brown");
        c3.setUser(user2);
        contactRepository.save(c3);
    }

    @Test
    void findByIdAndUser_shouldReturnContactWhenOwnedByUser() {
        Contact c = contactRepository.findByUser(user1).get(0);
        Optional<Contact> found = contactRepository.findByIdAndUser(c.getId(), user1);
        assertTrue(found.isPresent());
    }

    @Test
    void findByIdAndUser_shouldReturnEmptyWhenOwnedByOtherUser() {
        Contact c = contactRepository.findByUser(user1).get(0);
        Optional<Contact> found = contactRepository.findByIdAndUser(c.getId(), user2);
        assertFalse(found.isPresent());
    }

    @Test
    void findByUser_shouldReturnOnlyUserContacts() {
        List<Contact> contacts = contactRepository.findByUser(user1);
        assertEquals(2, contacts.size());
        assertTrue(contacts.stream().allMatch(c -> c.getUser().equals(user1)));
    }

    @Test
    void findByUser_shouldReturnEmptyForUserWithNoContacts() {
        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setPhone("3333333333");
        user3.setPasswordHash("hash3");
        userRepository.save(user3);

        List<Contact> contacts = contactRepository.findByUser(user3);
        assertTrue(contacts.isEmpty());
    }

    @Test
    void search_shouldFindContactsByNameIgnoringCase() {
        List<Contact> results = contactRepository
                .findByUserAndFirstNameContainingIgnoreCaseOrUserAndLastNameContainingIgnoreCase(
                        user1, "alice", user1, "alice");
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getFirstName());
    }

    @Test
    void search_shouldReturnEmptyWhenNoMatch() {
        List<Contact> results = contactRepository
                .findByUserAndFirstNameContainingIgnoreCaseOrUserAndLastNameContainingIgnoreCase(
                        user1, "zzz", user1, "zzz");
        assertTrue(results.isEmpty());
    }

    @Test
    void pagination_shouldReturnCorrectPage() {
        Page<Contact> page = contactRepository.findByUser(user1, PageRequest.of(0, 1));
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void pagination_shouldReturnEmptyForOutOfBoundsPage() {
        Page<Contact> page = contactRepository.findByUser(user1, PageRequest.of(10, 10));
        assertTrue(page.getContent().isEmpty());
    }
}