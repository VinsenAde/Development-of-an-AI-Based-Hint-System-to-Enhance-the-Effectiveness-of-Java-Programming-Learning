package com.thesis.java.javalearning.repository;

import com.thesis.java.javalearning.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    // Additional custom queries can be added here, if needed.
}
