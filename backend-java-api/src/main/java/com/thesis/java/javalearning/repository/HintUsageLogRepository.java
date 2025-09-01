package com.thesis.java.javalearning.repository;

import com.thesis.java.javalearning.entity.HintUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HintUsageLogRepository extends JpaRepository<HintUsageLog, Long> {
}
