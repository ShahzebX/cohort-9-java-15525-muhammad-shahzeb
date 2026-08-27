package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.Contact;
import com.example.contactmanagementsystem.ContactRepository;
import com.example.contactmanagementsystem.ContactService;
import com.example.contactmanagementsystem.Phone;
import com.example.contactmanagementsystem.Email;
import com.example.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact contact;

    @BeforeEach
    void setUp() {
        contact = new Contact();
        contact.setId(1);
        contact.setFirstName("John");
        contact.setLastName("Doe");
    }

    @Test
    void createContact_savesSuccessfully() {
        when(contactRepository.save(contact)).thenReturn(contact);

        Contact result = contactService.createContact(contact);

        assertEquals(contact, result);
        verify(contactRepository).save(contact);
    }

    @Test
    void getContactById_returnsContact_whenFound() {
        when(contactRepository.findById(1)).thenReturn(Optional.of(contact));

        Contact result = contactService.getContactById(1);

        assertEquals(contact, result);
    }

    @Test
    void getContactById_throwsNotFound_whenMissing() {
        when(contactRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(99));
    }

    @Test
    void deleteContact_callsRepository() {
        contactService.deleteContact(1);

        verify(contactRepository).deleteById(1);
    }

    @Test
    void searchContact_returnsMatchingList() {
        when(contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "john"))
                .thenReturn(List.of(contact));

        List<Contact> result = contactService.searchContact("john");

        assertEquals(1, result.size());
        assertTrue(result.contains(contact));
    }

    @Test
    void updateContact_updatesFieldsAndSyncsPhonesEmails() {
        Contact updatedData = new Contact();
        updatedData.setFirstName("Jane");
        updatedData.setLastName("Smith");
        updatedData.setTitle("Manager");

        Phone newPhone = new Phone();
        newPhone.setPhoneNumber("03001234567");
        updatedData.getPhones().add(newPhone);

        Email newEmail = new Email();
        newEmail.setEmailAddress("jane@example.com");
        updatedData.getEmails().add(newEmail);

        when(contactRepository.findById(1)).thenReturn(Optional.of(contact));
        when(contactRepository.save(contact)).thenReturn(contact);

        Contact result = contactService.updateContact(1, updatedData);

        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Manager", result.getTitle());
        assertEquals(1, result.getPhones().size());
        assertEquals("03001234567", result.getPhones().get(0).getPhoneNumber());
        assertEquals(1, result.getEmails().size());
        assertEquals("jane@example.com", result.getEmails().get(0).getEmailAddress());
        verify(contactRepository).save(contact);
    }

    @Test
    void updateContact_throwsNotFound_whenMissing() {
        Contact updatedData = new Contact();
        when(contactRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contactService.updateContact(99, updatedData));
    }
}