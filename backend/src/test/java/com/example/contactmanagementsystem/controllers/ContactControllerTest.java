package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.Contact;
import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.dto.ContactRequest;
import com.example.contactmanagementsystem.services.ContactService;
import com.example.contactmanagementsystem.services.UserService;
import com.example.contactmanagementsystem.security.JwtUtil;
import com.example.contactmanagementsystem.security.CustomUserDetailsService;
import com.example.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        return user;
    }

    private Contact mockContact() {
        Contact c = new Contact();
        c.setId(1);
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setUser(mockUser());
        return c;
    }

    private TestingAuthenticationToken auth() {
        return new TestingAuthenticationToken("test@example.com", null);
    }

    @Test
    void getAllContacts_shouldReturnList() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.getAllContacts(any())).thenReturn(List.of(mockContact()));

        mockMvc.perform(get("/api/contacts").principal(auth()))
                .andExpect(status().isOk());
    }

    @Test
    void getContactById_shouldReturnContact() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.getContactById(eq(1), any())).thenReturn(mockContact());

        mockMvc.perform(get("/api/contacts/1").principal(auth()))
                .andExpect(status().isOk());
    }

    @Test
    void getContactById_shouldThrow404() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.getContactById(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Contact does not exist"));

        mockMvc.perform(get("/api/contacts/99").principal(auth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createContact_shouldReturn200() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.createContact(any(ContactRequest.class), any())).thenReturn(mockContact());

        mockMvc.perform(post("/api/contacts")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateContact_shouldReturn200() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.updateContact(eq(1), any(ContactRequest.class), any())).thenReturn(mockContact());

        mockMvc.perform(put("/api/contacts/1")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteContact_shouldReturn200() throws Exception {
        User user = mockUser();
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(user);

        mockMvc.perform(delete("/api/contacts/1").with(authentication(auth())))
                .andExpect(status().isOk());

        verify(contactService).deleteContact(1, user);
    }

    @Test
    void searchContacts_shouldReturnList() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.searchContact(eq("John"), any())).thenReturn(List.of(mockContact()));

        mockMvc.perform(get("/api/contacts/search").param("query", "John").principal(auth()))
                .andExpect(status().isOk());
    }

    @Test
    void paginated_shouldReturnPage() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        Page<Contact> page = new PageImpl<>(List.of(mockContact()));
        when(contactService.getContactsPaginated(eq(0), eq(10), any())).thenReturn(page);

        mockMvc.perform(get("/api/contacts/paginated")
                        .param("page", "0").param("size", "10")
                        .principal(auth()))
                .andExpect(status().isOk());
    }

    @Test
    void paginated_shouldThrow400OnNegativePage() throws Exception {
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.getContactsPaginated(eq(-1), eq(10), any()))
                .thenThrow(new IllegalArgumentException("Page number must not be negative"));

        mockMvc.perform(get("/api/contacts/paginated")
                        .param("page", "-1").param("size", "10")
                        .principal(auth()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateContact_shouldReturn409OnDataIntegrityViolation() throws Exception {
        // Simulate the DuplicateResourceException that ContactService.updateContact
        // now translates from DataIntegrityViolationException inside the saveAndFlush
        // catch block, ensuring flush-time constraint failures surface as HTTP 409.
        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(mockUser());
        when(contactService.updateContact(eq(1), any(ContactRequest.class), any()))
                .thenThrow(new com.example.exception.DuplicateResourceException(
                        "Contact update violates data constraints."));

        mockMvc.perform(put("/api/contacts/1")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\"}"))
                .andExpect(status().isConflict());
    }
}