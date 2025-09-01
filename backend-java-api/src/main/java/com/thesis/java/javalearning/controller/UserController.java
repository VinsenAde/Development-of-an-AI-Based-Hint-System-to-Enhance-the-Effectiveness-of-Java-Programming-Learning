package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.repository.UserRepository;
import com.thesis.java.javalearning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/users", produces = "application/json")
public class UserController {

    private final UserRepository userRepository;
    private final UserService    userService;

    public UserController(UserRepository userRepository,
                          UserService userService) {
        this.userRepository = userRepository;
        this.userService    = userService;
    }

    /**
     * 1) Fetch the current logged-in user’s details.
     *    GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String         name = auth.getName();

        return userRepository.findByUsername(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 2) Fetch the saved AI feedback for the current user.
     *    GET /api/users/me/feedback
     */
 @GetMapping(path = "/me/feedback", produces = "application/json")
public ResponseEntity<Map<String,String>> getMyFeedback(Principal principal) {
    String username = principal.getName();
    User u = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    // Get the feedback, providing an empty string if it's null
    String feedback = u.getAiFeedback();
    String safeFeedback = (feedback != null) ? feedback : "";

    return ResponseEntity.ok(Map.of("feedback", safeFeedback));
}
    /**
     * 3) Fetch saved AI feedback for any user by ID (ADMIN only).
     *    GET /api/users/{id}/feedback
     */
    @GetMapping(path = "/{id}/feedback", produces = "application/json")
    public ResponseEntity<Map<String,String>> getFeedbackForUser(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No user with id " + id));
        return ResponseEntity.ok(Map.of("feedback", u.getAiFeedback()));
    }

    /**
     * 4) Save AI feedback into the current user’s aiFeedback field.
     *    POST /api/users/me/feedback
     *    Body: { "feedback": "…" }
     */
    @PostMapping(path = "/me/feedback", consumes = "application/json")
    public ResponseEntity<Void> saveMyFeedback(@RequestBody Map<String,String> body,
                                               Principal principal) {
        String fb = body.get("feedback");
        if (fb == null || fb.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userService.saveFeedbackForUsername(principal.getName(), fb);
        return ResponseEntity.ok().build();
    }

    /**
     * 5) Save AI feedback into *any* user’s aiFeedback field.
     *    POST /api/users/{id}/feedback
     *    Body: { "feedback": "…" }
     *    (ADMIN-only via SecurityConfig)
     */
    @PostMapping(path = "/{id}/feedback", consumes = "application/json")
    public ResponseEntity<Void> saveFeedbackForUser(
        @PathVariable("id") Long userId,
        @RequestBody Map<String,String> body
    ) {
        String fb = body.get("feedback");
        if (fb == null || fb.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userService.saveFeedbackForUserId(userId, fb);
        return ResponseEntity.ok().build();
    }

    /**
     * 6) Delete the saved AI feedback for the given user.
     *    DELETE /api/users/{id}/feedback
     */
    @DeleteMapping(path = "/{id}/feedback")
    public ResponseEntity<Void> deleteFeedbackForUser(
        @PathVariable("id") Long userId
    ) {
        userService.deleteFeedbackForUserId(userId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/me/feedback")
public ResponseEntity<Void> deleteMyFeedback(Principal principal) {
    String username = principal.getName();
    userService.deleteFeedbackForUsername(username);
    return ResponseEntity.ok().build();
}

}
