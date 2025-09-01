package com.thesis.java.javalearning.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "app_user")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 private String fullName;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; // Default ROLE is USER

    @Lob
    private String aiFeedback;

    // Empty constructor
    public User() {}

    // Constructor for username and password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = Role.USER; // Default to USER role
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public enum Role {
        ADMIN, USER
    }
}
