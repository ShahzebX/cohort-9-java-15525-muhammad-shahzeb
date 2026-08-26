package com.example.contactmanagementsystem;

import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user){
        boolean noPhone = user.getPhone() == null || user.getPhone().isBlank();
        boolean noEmail = user.getEmail() == null || user.getEmail().isBlank();

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank())
            throw new IllegalArgumentException("Password must not be blank");

        if (noPhone && noEmail)
            throw new IllegalArgumentException("User must register using either Email or Phone");

        if (!noPhone && userRepository.findByPhone(user.getPhone()).isPresent())
            throw new DuplicateResourceException("Phone already in use: " + user.getPhone());

        if (!noEmail && userRepository.findByEmail(user.getEmail()).isPresent())
            throw new DuplicateResourceException("Email already in use: " + user.getEmail());

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Email or phone already in use.");
        }
    }

    public User findByEmailOrPhone(String identifier){
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("No user found with: " + identifier));
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword){
        if(newPassword.isBlank())
            throw new IllegalArgumentException("New password cannot be blank");

        if(oldPassword.isBlank())
            throw new IllegalArgumentException("Old password cannot be blank");

        if(newPassword.equals(oldPassword))
            throw new IllegalArgumentException("New password cannot be the same as the old password");

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean passwordMatch = passwordEncoder.matches(oldPassword, user.getPasswordHash());

        if (!passwordMatch)
            throw new IllegalArgumentException("Old password is incorrect");

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}
