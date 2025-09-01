package com.thesis.java.javalearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionResult {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "job_id", nullable = false, updatable = false)
    private UUID jobId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ExecutionJob executionJob;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "stdout", columnDefinition = "TEXT")
    private String stdout;

    @Column(name = "stderr", columnDefinition = "TEXT")
    private String stderr;

    @Column(name = "exit_code")
    private Integer exitCode;

    @Column(name = "score_calculated")
    private Integer scoreCalculated;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}
