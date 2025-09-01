package com.thesis.java.javalearning.service;

import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with the given username, full name, raw password, and role.
     */
    @Transactional
    public void register(String username,
                         String fullName,
                         String rawPassword,
                         User.Role role) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFullName(fullName);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRole(role);
        userRepository.save(newUser);
    }

    /**
     * Lookup a User by username, or throw if not found.
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Save AI-generated feedback into the `aiFeedback` column for the given username.
     */
    @Transactional
    public void saveFeedbackForUsername(String username, String feedback) {
        User user = findByUsername(username);
        user.setAiFeedback(feedback);
    }
    
        @Transactional
    public void saveFeedbackForUserId(Long userId, String feedback) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));
        user.setAiFeedback(feedback);
        // optional, but explicit:
        userRepository.save(user);
    }
    
        @Transactional
    public void deleteFeedbackForUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));
        user.setAiFeedback(null);
        userRepository.save(user);
    }
    @Transactional
public void deleteFeedbackForUsername(String username) {
    User user = findByUsername(username);
    user.setAiFeedback(null);
}

}
