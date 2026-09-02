package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.*;
import com.example.contactmanagementsystem.dto.ContactRequest;
import com.example.contactmanagementsystem.dto.EmailRequest;
import com.example.contactmanagementsystem.dto.PhoneRequest;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    public static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private ContactRepository contactRepository;

    public Contact createContact(ContactRequest request, User user){
        if (request == null)
            throw new IllegalArgumentException("Contact data must not be null");
        if (user == null)
            throw new IllegalArgumentException("Contact owner must not be null");
        if (containsNull(request.getPhones()))
            throw new IllegalArgumentException("Phones must not contain null values");
        if (containsNull(request.getEmails()))
            throw new IllegalArgumentException("Emails must not contain null values");

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setUser(user);

        if (request.getEmails() != null) {
            for (EmailRequest emailRequest : request.getEmails()) {
                contact.addEmail(toEmail(emailRequest));
            }
        }

        if (request.getPhones() != null) {
            for (PhoneRequest phoneRequest : request.getPhones()) {
                contact.addPhone(toPhone(phoneRequest));
            }
        }

        try {
            Contact saved = contactRepository.save(contact);
            logger.info("Created contact id={} for user id={}", saved.getId(), user.getId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            logger.warn("Contact creation failed for user id={}", user.getId());
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
        logger.info("Deleted contact id={} for user id={}", id, user.getId());
    }

    @Transactional
    public Contact updateContact(Integer id, ContactRequest request, User user){
        Contact existingContact = contactRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Contact does not exist!"));

        if (request == null)
            throw new IllegalArgumentException("Updated data must not be null");

        // Validate ALL provided input BEFORE mutating the existing contact so a
        // validation failure leaves it completely unchanged.
        if (containsNull(request.getPhones()))
            throw new IllegalArgumentException("Phones must not contain null values");
        if (containsNull(request.getEmails()))
            throw new IllegalArgumentException("Emails must not contain null values");

        // Apply scalar fields only when explicitly provided (null = omit), so
        // stored values are preserved for omitted fields — same partial-update
        // semantics as the nested collections below.
        if (request.getFirstName() != null) {
            existingContact.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existingContact.setLastName(request.getLastName());
        }
        if (request.getTitle() != null) {
            existingContact.setTitle(request.getTitle());
        }

        // Replace a nested collection ONLY when explicitly provided; when omitted,
        // the existing associations are preserved (partial update).
        if (request.getPhones() != null) {
            replacePhones(existingContact, request.getPhones());
        }

        if (request.getEmails() != null) {
            replaceEmails(existingContact, request.getEmails());
        }

        Contact saved = contactRepository.save(existingContact);
        logger.info("Updated contact id={} for user id={}", id, user.getId());
        return saved;
    }

    private Email toEmail(EmailRequest request) {
        Email email = new Email();
        email.setEmail(request.getEmail());
        email.setLabel(request.getLabel());
        return email;
    }

    private Phone toPhone(PhoneRequest request) {
        Phone phone = new Phone();
        phone.setPhoneNumber(request.getPhoneNumber());
        phone.setLabel(request.getLabel());
        return phone;
    }

    private boolean containsNull(List<?> list) {
        if (list == null)
            return false;
        for (Object item : list) {
            if (item == null)
                return true;
        }
        return false;
    }

    private void replacePhones(Contact contact, List<PhoneRequest> newPhones) {
        List<Phone> existing = new ArrayList<>(contact.getPhones());
        for (Phone phone : existing) {
            contact.removePhone(phone);
        }
        for (PhoneRequest request : newPhones) {
            contact.addPhone(toPhone(request));
        }
    }

    private void replaceEmails(Contact contact, List<EmailRequest> newEmails) {
        List<Email> existing = new ArrayList<>(contact.getEmails());
        for (Email email : existing) {
            contact.removeEmail(email);
        }
        for (EmailRequest request : newEmails) {
            contact.addEmail(toEmail(request));
        }
    }

    public List<Contact> searchContact(String query, User user){
        return contactRepository.findByUserAndFirstNameContainingIgnoreCaseOrUserAndLastNameContainingIgnoreCase(user, query, user, query);
    }

    public Page<Contact> getContactsPaginated(int page, int size, User user){
        if(page < 0)
            throw new IllegalArgumentException("Page number must not be negative");
        if(size <= 0)
            throw new IllegalArgumentException("Page size must be greater than 0");
        if(size > MAX_PAGE_SIZE)
            throw new IllegalArgumentException("Page size must not exceed " + MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findByUser(user, pageable);
    }

}
