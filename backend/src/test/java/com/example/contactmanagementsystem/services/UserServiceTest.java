package com.example.contactmanagementsystem.services;

import com.example.contactmanagementsystem.User;
import com.example.contactmanagementsystem.UserRepository;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setPhone("1234567890");
        // Must satisfy enforcePasswordPolicy: ≥8 chars, ≥1 letter, ≥1 digit.
        user.setPasswordHash("rawPass1");
    }

    @Test
    void registerUser_shouldEncodePasswordAndSave() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(user);

        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowWhenNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null));
    }

    @Test
    void registerUser_shouldThrowWhenNoEmailAndNoPhone() {
        User noContact = new User();
        // Policy-valid password so the test reaches the email/phone check, not the policy check.
        noContact.setPasswordHash("passWord1");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(noContact));
    }

    @Test
    void registerUser_shouldThrowWhenPhoneExists() {
        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(user));

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(user));
    }

    @Test
    void registerUser_shouldThrowWhenEmailExists() {
        when(userRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(DuplicateResourceException.class, () -> userService.registerUser(user));
    }

    @Test
    void findByEmailOrPhone_shouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmailOrPhone("test@example.com");

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void findByEmailOrPhone_shouldThrowWhenNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findByEmailOrPhone("unknown@example.com"));
    }

    @Test
    void findByEmailOrPhone_shouldThrowWhenBlank() {
        assertThrows(IllegalArgumentException.class, () -> userService.findByEmailOrPhone(""));
    }

    @Test
    void changePassword_shouldEncodeAndSave() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        // "oldPass1" matches the stored hash; "newPass1" satisfies enforcePasswordPolicy.
        when(passwordEncoder.matches("oldPass1", "rawPass1")).thenReturn(true);
        when(passwordEncoder.encode("newPass1")).thenReturn("encodedNewPass");

        userService.changePassword(1, "oldPass1", "newPass1");

        verify(passwordEncoder).encode("newPass1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_shouldThrowWhenOldPasswordIncorrect() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        // "wrongPass1" satisfies enforcePasswordPolicy for newPassword so the test
        // reaches the passwordEncoder.matches check — the intended assertion point.
        when(passwordEncoder.matches("wrongPass", "rawPass1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1, "wrongPass", "newPass1"));
    }
}
