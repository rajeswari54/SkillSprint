package com.example.demo.Repository;

import com.example.demo.Entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for StudentAnswer entity.
 */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /**
     * All pending answers waiting for mentor evaluation.
     * Used by LINGUIST/ADMIN to see what needs grading.
     */
    List<StudentAnswer> findByStatus(StudentAnswer.AnswerStatus status);

    /**
     * All answers submitted by a specific student.
     * Used by student to see their own history and scores.
     */
    List<StudentAnswer> findByUserId(Long userId);

    /**
     * All pending answers for a specific deck.
     * Mentor can filter by their own deck.
     */
    List<StudentAnswer> findByDeckIdAndStatus(Long deckId, StudentAnswer.AnswerStatus status);
}