package com.example.contactmanagementsystem;

import org.springframework.beans.factory.annotation.Autowired;
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
        return contactRepository.findById(id).orElse(null);
    }

    public void deleteContact(Integer id){
        contactRepository.deleteById(id);
    }
}
