package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.*;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact createContact(Contact contact, User user){
        contact.setUser(user);

        if (contact.getEmails() != null) {
            List<Email> emails = new ArrayList<>(contact.getEmails());
            for (Email email : emails) {
                contact.addEmail(email);
            }
        }

        if (contact.getPhones() != null) {
            List<Phone> phones = new ArrayList<>(contact.getPhones());
            for (Phone phone : phones) {
                contact.addPhone(phone);
            }
        }

        try {
            return contactRepository.save(contact);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Contact already exists or violates data constraints.", e);
        }
    }

    public List<Contact> getAllContacts(User user){
        return contactRepository.findByUser(user);
    }

    public Contact getContactById(Integer id, User user){
        return contactRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Contact does not exist!"));
    }

    public void deleteContact(Integer id, User user){
        Contact contact = contactRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Contact does not exist!"));
        contactRepository.delete(contact);
    }

    @Transactional
    public Contact updateContact(Integer id, Contact updatedData, User user){
        Contact existingContact = contactRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Contact does not exist!"));

        if (updatedData == null)
            throw new IllegalArgumentException("Updated data must not be null");

        existingContact.setFirstName(updatedData.getFirstName());
        existingContact.setLastName(updatedData.getLastName());
        existingContact.setTitle(updatedData.getTitle());

        if (updatedData.getPhones() == null || updatedData.getEmails() == null)
            throw new IllegalArgumentException("Phones and emails must not be null");

        List<Phone> existingPhones = new ArrayList<>(existingContact.getPhones());
        for (Phone phone : existingPhones) {
            existingContact.removePhone(phone);
        }
        updatedData.getPhones().forEach(existingContact::addPhone);

        List<Email> existingEmails = new ArrayList<>(existingContact.getEmails());
        for (Email email : existingEmails) {
            existingContact.removeEmail(email);
        }
        updatedData.getEmails().forEach(existingContact::addEmail);

        return contactRepository.save(existingContact);
    }

    public List<Contact> searchContact(String query, User user){
        return contactRepository.findByUserAndFirstNameContainingIgnoreCaseOrUserAndLastNameContainingIgnoreCase(user, query, user, query);
    }

    public Page<Contact> getContactsPaginated(int page, int size, User user){
        if(page < 0)
            throw new IllegalArgumentException("Page number must not be negative");
        if(size <= 0)
            throw new IllegalArgumentException("Page size must be greater than 0");
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findByUser(user, pageable);
    }

}
