package com.example.contactmanagementsystem;

import com.example.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact createContact(Contact contact){
        return contactRepository.save(contact);
    }

    public List<Contact> getAllContacts(){
        return contactRepository.findAll();
    }

    public Contact getContactById(Integer id){
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }

    public void deleteContact(Integer id){
        contactRepository.deleteById(id);
    }

    public Contact updateContact(Integer id, Contact updatedData){
        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Contact does not exist!"));

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
        return contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }

    public Page<Contact> getContactsPaginated(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return contactRepository.findAll(pageable);
    }

}
