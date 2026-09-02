package com.example.contactmanagementsystem.controllers;

import com.example.contactmanagementsystem.dto.ChangePasswordRequest;
import com.example.contactmanagementsystem.dto.LoginRequest;
import com.example.contactmanagementsystem.dto.RegisterRequest;
import com.example.contactmanagementsystem.dto.AuthResponse;
import com.example.contactmanagementsystem.services.UserService;
import com.example.contactmanagementsystem.security.JwtUtil;
import com.example.contactmanagementsystem.security.CustomUserDetailsService;
import com.example.exception.InvalidCredentialsException;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_shouldReturnToken() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "test@example.com", null, "password123");
        when(userService.registerUser(any())).thenReturn(new com.example.contactmanagementsystem.User());
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void register_shouldThrow409OnDuplicate() throws Exception {
        when(userService.registerUser(any())).thenThrow(new DuplicateResourceException("Email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void register_shouldThrow400OnMissingCredentials() throws Exception {
        when(userService.registerUser(any())).thenThrow(new IllegalArgumentException("User must register using either Email or Phone"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400OnWeakPassword() throws Exception {
        // A password without digits violates the shared policy (letters + digits + minLength).
        // UserService.enforcePasswordPolicy throws IllegalArgumentException → HTTP 400.
        when(userService.registerUser(any()))
                .thenThrow(new IllegalArgumentException(
                        "Password must be at least 8 characters and contain both letters and digits."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\","
                                + "\"email\":\"test@example.com\",\"password\":\"weakpass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnProfileFields() throws Exception {
        com.example.contactmanagementsystem.User user = new com.example.contactmanagementsystem.User();
        user.setId(1);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPhone("+15550001111");
        user.setPasswordHash("encoded");

        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.identifier").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phone").value("+15550001111"));
    }

    @Test
    void login_shouldThrow401OnWrongPassword() throws Exception {
        com.example.contactmanagementsystem.User user = new com.example.contactmanagementsystem.User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setPasswordHash("encoded");

        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"test@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldThrow401OnUnknownUser() throws Exception {
        when(userService.findByEmailOrPhone("unknown@example.com"))
                .thenThrow(new ResourceNotFoundException("No user found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"unknown@example.com\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        com.example.contactmanagementsystem.User user = new com.example.contactmanagementsystem.User();
        user.setId(1);
        user.setEmail("test@example.com");

        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(user);
        doNothing().when(userService).changePassword(eq(1), eq("oldPass"), eq("newPass"));

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"oldPass\",\"newPassword\":\"newPass\"}")
                        .principal(new org.springframework.security.authentication.TestingAuthenticationToken(
                                "test@example.com", null)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_shouldThrow400OnOldPasswordMismatch() throws Exception {
        com.example.contactmanagementsystem.User user = new com.example.contactmanagementsystem.User();
        user.setId(1);
        user.setEmail("test@example.com");

        when(userService.findByEmailOrPhone("test@example.com")).thenReturn(user);
        doThrow(new IllegalArgumentException("Old password is incorrect"))
                .when(userService).changePassword(eq(1), eq("wrong"), eq("newPass"));

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"wrong\",\"newPassword\":\"newPass\"}")
                        .principal(new org.springframework.security.authentication.TestingAuthenticationToken(
                                "test@example.com", null)))
                .andExpect(status().isBadRequest());
    }
}