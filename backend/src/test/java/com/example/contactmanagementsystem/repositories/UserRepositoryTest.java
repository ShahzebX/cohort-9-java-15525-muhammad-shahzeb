package com.example.contactmanagementsystem.repositories;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPhone("1111111111");
        user.setPasswordHash("hash");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotFound() {
        assertTrue(userRepository.findByEmail("nonexistent@example.com").isEmpty());
    }

    @Test
    void findByPhone_shouldReturnUser() {
        User user = new User();
        user.setEmail("phoneuser@example.com");
        user.setPhone("2222222222");
        user.setPasswordHash("hash");
        userRepository.save(user);

        Optional<User> found = userRepository.findByPhone("2222222222");
        assertTrue(found.isPresent());
        assertEquals("2222222222", found.get().getPhone());
    }

    @Test
    void findByPhone_shouldReturnEmptyWhenNotFound() {
        assertTrue(userRepository.findByPhone("0000000000").isEmpty());
    }

    @Test
    void save_shouldPersistAllFields() {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail("jane@example.com");
        user.setPhone("3333333333");
        user.setPasswordHash("encoded");
        // saveAndFlush forces a SQL INSERT immediately; clear() evicts the entity
        // from the first-level cache so the following findById issues a real SELECT
        // rather than returning the in-memory instance.
        User saved = userRepository.saveAndFlush(user);
        entityManager.clear();

        User fetched = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Jane", fetched.getFirstName());
        assertEquals("Smith", fetched.getLastName());
        assertNotNull(fetched.getCreatedAt());
    }
}
