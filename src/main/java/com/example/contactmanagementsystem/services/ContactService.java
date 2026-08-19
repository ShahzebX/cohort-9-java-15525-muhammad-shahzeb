package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.Contact;
import com.example.contactmanagementsystem.ContactRepository;
import com.example.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ContactService {
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    @Autowired
    private ContactRepository contactRepository;

    public Contact createContact(Contact contact){
        logger.info("Creating new contact: {} {}", contact.getFirstName(), contact.getLastName());
        return contactRepository.save(contact);
    }

    public List<Contact> getAllContacts(){
        return contactRepository.findAll();
    }

    public Contact getContactById(Integer id){
        return contactRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Contact not found with id: {}", id);
                    return new ResourceNotFoundException("Contact not found with id: " + id);
                });
    }

    public void deleteContact(Integer id){
        logger.info("Deleting contact with id: {}", id);
        contactRepository.deleteById(id);
    }

    public Contact updateContact(Integer id, Contact updatedData){
        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Update failed — contact not found with id: {}", id);
                    return new ResourceNotFoundException("Contact not found with id: " + id);
                });

        existingContact.setFirstName(updatedData.getFirstName());
        existingContact.setLastName(updatedData.getLastName());
        existingContact.setTitle(updatedData.getTitle());

        existingContact.getPhones().clear();
        updatedData.getPhones()
                .forEach(phone -> {
            existingContact.addPhone(phone);
                }
        );

        existingContact.getEmails().clear();
        updatedData.getEmails().forEach(existingContact::addEmail);

        return contactRepository.save(existingContact);
    }

    public List<Contact> searchContact(String query){
        logger.debug("Searching contacts with query: {}", query);
        return contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }

    public Page<Contact> getContactsPaginated(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable);
    }

}
