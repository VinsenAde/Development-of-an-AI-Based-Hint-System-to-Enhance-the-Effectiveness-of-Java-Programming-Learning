package com.thesis.java.javalearning.repository;

import com.thesis.java.javalearning.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    // Define custom queries if needed later.
}
