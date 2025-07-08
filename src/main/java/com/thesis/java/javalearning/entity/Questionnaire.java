package com.thesis.java.javalearning.entity;

import com.thesis.java.javalearning.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // We assume a very basic user association; in a full implementation, expand user management.
    @ManyToOne
    private User user;
    
    // Indicates which session this feedback is for (e.g., 1 for pre-test, 2 for post-test)
    private int sessionNumber;
    
    // Scores are on a scale of 1 to 10.
    private int understandingScore;
    private int confidenceScore;
    
    // Free text feedback from the student.
    private String comments;

    public Questionnaire() {}

    public Questionnaire(User user, int sessionNumber, int understandingScore, int confidenceScore, String comments) {
        this.user = user;
        this.sessionNumber = sessionNumber;
        this.understandingScore = understandingScore;
        this.confidenceScore = confidenceScore;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public int getUnderstandingScore() { return understandingScore; }
    public void setUnderstandingScore(int understandingScore) { this.understandingScore = understandingScore; }

    public int getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(int confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
