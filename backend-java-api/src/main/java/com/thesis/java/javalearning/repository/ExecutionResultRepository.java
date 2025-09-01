package com.thesis.java.javalearning.repository;

import com.thesis.java.javalearning.entity.ExecutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, UUID> {
    Optional<ExecutionResult> findByJobId(UUID jobId);
}
