package com.thesis.java.javalearning.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class HintUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hintType; // syntax, logic, step, reveal
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private int sessionNumber;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHintType() { return hintType; }
    public void setHintType(String hintType) { this.hintType = hintType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
}
