package com.thesis.java.javalearning.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String expectedOutput;   // The correct output for a task
    
    private String category;         // High-level topic (e.g., "Basic Syntax")
    private String subTopic;         // More granular (e.g., "Print Statement")
    
    public Problem() { }
    
    public Problem(String title, String description, String expectedOutput, String category, String subTopic) {
        this.title = title;
        this.description = description;
        this.expectedOutput = expectedOutput;
        this.category = category;
        this.subTopic = subTopic;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getExpectedOutput() {
        return expectedOutput;
    }
    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getSubTopic() {
        return subTopic;
    }
    public void setSubTopic(String subTopic) {
        this.subTopic = subTopic;
    }
}
