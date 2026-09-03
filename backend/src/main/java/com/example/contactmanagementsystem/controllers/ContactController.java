package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.Contact;
import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.dto.ContactRequest;
import com.example.contactmanagementsystem.services.ContactService;
import com.example.contactmanagementsystem.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    private User getAuthenticatedUser(Authentication authentication) {
        String identifier = authentication.getName();
        return userService.findByEmailOrPhone(identifier);
    }

    @PostMapping
    public Contact createContact(@Valid @RequestBody ContactRequest request, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return contactService.createContact(request, user);
    }

    @GetMapping
    public List<Contact> getAllContacts(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return contactService.getAllContacts(user);
    }

    @GetMapping("/{id}")
    public Contact getContactById(@PathVariable Integer id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return contactService.getContactById(id, user);
    }

    @PutMapping("/{id}")
    public Contact updateContact(@PathVariable Integer id, @Valid @RequestBody ContactRequest request, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return contactService.updateContact(id, request, user);
    }

    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable Integer id, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        contactService.deleteContact(id, user);
    }

    @GetMapping("/search")
    public List<Contact> searchContacts(@RequestParam String query, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return contactService.searchContact(query, user);
    }

    @GetMapping("/paginated")
    public Page<Contact> getContactsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return contactService.getContactsPaginated(page, size, user);
    }
}
