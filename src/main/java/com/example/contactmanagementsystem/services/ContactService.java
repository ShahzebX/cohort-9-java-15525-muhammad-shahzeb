package com.example.contactmanagementsystem;

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

    public Contact createContact(Contact contact){
        try {
            return contactRepository.save(contact);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Contact already exists or violates data constraints.", e);
        }
    }

    public List<Contact> getAllContacts(){
        return contactRepository.findAll();
    }

    public Contact getContactById(Integer id){
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Contact does not exist!"));
    }

    public void deleteContact(Integer id){
        if (!contactRepository.existsById(id))
            throw new ResourceNotFoundException("Error: Contact does not exist!");
        contactRepository.deleteById(id);
    }

    @Transactional
    public Contact updateContact(Integer id, Contact updatedData){
        Contact existingContact = contactRepository.findById(id)
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

    public List<Contact> searchContact(String query){
        return contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }

    public Page<Contact> getContactsPaginated(int page, int size){
        if(page < 0)
            throw new IllegalArgumentException("Page number must not be negative");
        if(size <= 0)
            throw new IllegalArgumentException("Page size must be greater than 0");
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable);
    }

}
