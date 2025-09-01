package com.thesis.java.javalearning.repository;

import com.thesis.java.javalearning.entity.ExecutionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExecutionJobRepository extends JpaRepository<ExecutionJob, UUID> {
}
