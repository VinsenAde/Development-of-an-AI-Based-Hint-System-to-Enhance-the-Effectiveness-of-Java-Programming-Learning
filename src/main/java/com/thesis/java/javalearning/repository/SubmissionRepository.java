package com.thesis.java.javalearning.repository;

import com.thesis.java.javalearning.entity.Submission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // 🔹 Fetch submissions by user and session — used for regular users
    @EntityGraph(attributePaths = {"problem"})
    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.sessionNumber = :sessionNumber")
    List<Submission> findByUserIdAndSessionNumber(
        @Param("userId") Long userId,
        @Param("sessionNumber") int sessionNumber
    );

    // 🔹 Fetch all submissions for a given user (across sessions) — used for admin view
    @EntityGraph(attributePaths = {"problem"})
    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId")
    List<Submission> findByUserId(@Param("userId") Long userId);

    // 🔹 Check if user already submitted a specific problem in a session
    boolean existsByUser_IdAndProblem_IdAndSessionNumber(Long userId, Long problemId, int sessionNumber);

    // 🔹 Delete all submissions by user in a specific session (used for resets)
    void deleteByUser_IdAndSessionNumber(Long userId, int sessionNumber);
}
