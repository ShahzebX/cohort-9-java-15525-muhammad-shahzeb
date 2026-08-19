package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.Contact;
import com.example.contactmanagementsystem.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public Contact createContact(@RequestBody Contact contact) {
        return contactService.createContact(contact);
    }

    @GetMapping
    public List<Contact> getAllContacts() {
        return contactService.getAllContacts();
    }

    @GetMapping("/{id}")
    public Contact getContactById(@PathVariable Integer id) {
        return contactService.getContactById(id);
    }

    @PutMapping("/{id}")
    public Contact updateContact(@PathVariable Integer id, @RequestBody Contact updatedData) {
        return contactService.updateContact(id, updatedData);
    }

    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable Integer id) {
        contactService.deleteContact(id);
    }

    @GetMapping("/search")
    public List<Contact> searchContacts(@RequestParam String query) {
        return contactService.searchContact(query);
    }

    @GetMapping("/paginated")
    public Page<Contact> getContactsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return contactService.getContactsPaginated(page, size);
    }
}