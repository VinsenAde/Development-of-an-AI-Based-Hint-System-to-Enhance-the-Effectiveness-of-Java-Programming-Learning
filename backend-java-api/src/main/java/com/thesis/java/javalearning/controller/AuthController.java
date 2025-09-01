//package com.thesis.java.javalearning.controller;
//
//import com.thesis.java.javalearning.dto.UserRegistrationRequestDTO;
//import com.thesis.java.javalearning.entity.User;
//import com.thesis.java.javalearning.repository.UserRepository;
//import com.thesis.java.javalearning.response.ApiResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;
//@PostMapping("/register")
//public ResponseEntity<ApiResponse<Void>> register(
//        @Valid @RequestBody UserRegistrationRequestDTO request) {
//
//    if (userRepository.existsByUsername(request.getUsername())) {
//        return ResponseEntity
//            .badRequest()
//            .body(ApiResponse.error("Username is already taken."));
//    }
//
//    if (!request.getPassword().equals(request.getConfirmPassword())) {
//        return ResponseEntity
//            .badRequest()
//            .body(ApiResponse.error("Passwords do not match."));
//    }
//
//    String hashed = passwordEncoder.encode(request.getPassword());
//    User user = new User();
//    user.setUsername(request.getUsername());
//    user.setPassword(hashed);
//    user.setRole(request.getRole());   // <— FIXED
//    userRepository.save(user);
//
//    return ResponseEntity
//            .status(HttpStatus.CREATED)
//            .body(ApiResponse.ok(null, "Registration successful. Please login."));
//}
//
//}
package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.dto.UserRegistrationRequestDTO;
import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.repository.UserRepository;
import com.thesis.java.javalearning.response.ApiResponse;
import com.thesis.java.javalearning.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles authentication-related endpoints, including user registration.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(UserRepository userRepository,
                          UserService userService) {
        this.userRepository = userRepository;
        this.userService    = userService;
    }

    /**
     * Register a new user. Expects JSON:
     * {
     *   "fullName":      "Alice Smith",
     *   "username":      "alice",
     *   "password":      "secret123",
     *   "confirmPassword":"secret123",
     *   "role":          "USER"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody UserRegistrationRequestDTO request) {

        // 1) Username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Username is already taken."));
        }

        // 2) Password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Passwords do not match."));
        }

        // 3) Delegate to service (which will hash password and save fullName)
        userService.register(
            request.getUsername(),
            request.getFullName(),
            request.getPassword(),
            request.getRole()
        );

        // 4) Return 201 Created
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Registration successful. Please login."));
    }
}
