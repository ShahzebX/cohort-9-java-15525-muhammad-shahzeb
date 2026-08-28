package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.*;
import com.example.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private User user;
    private Contact contact;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        contact = new Contact();
        contact.setId(1);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setUser(user);
    }

    @Test
    void createContact_shouldSetUserAndSave() {
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.createContact(new Contact(), user);

        assertEquals(user, result.getUser());
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void getContactById_shouldReturnContactWhenOwnedByUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));

        Contact result = contactService.getContactById(1, user);

        assertEquals("John", result.getFirstName());
    }

    @Test
    void getContactById_shouldThrowWhenNotFoundForUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(1, user));
    }

    @Test
    void deleteContact_shouldDeleteWhenOwnedByUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));

        contactService.deleteContact(1, user);

        verify(contactRepository).delete(contact);
    }

    @Test
    void deleteContact_shouldThrowWhenNotFoundForUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(1, user));
    }

    @Test
    void updateContact_shouldUpdateWhenOwnedByUser() {
        Contact updatedData = new Contact();
        updatedData.setFirstName("Jane");
        updatedData.setLastName("Smith");
        updatedData.setEmails(new java.util.ArrayList<>());
        updatedData.setPhones(new java.util.ArrayList<>());

        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.updateContact(1, updatedData, user);

        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void updateContact_shouldThrowWhenNotFoundForUser() {
        when(contactRepository.findByIdAndUser(1, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.updateContact(1, new Contact(), user));
    }
}
