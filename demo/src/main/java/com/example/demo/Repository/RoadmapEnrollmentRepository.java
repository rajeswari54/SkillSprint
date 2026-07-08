package com.example.demo.Repository;

import com.example.demo.Entity.RoadmapEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RoadmapEnrollment entity.
 * Handles spaced-repetition metric queries per student.
 */
@Repository
public interface RoadmapEnrollmentRepository extends JpaRepository<RoadmapEnrollment, Long> {

    /**
     * Fetches all milestones due for review for a specific student.
     * A milestone is "due" when its nextReviewDate is on or before now.
     *
     * @param userId       the student's ID
     * @param currentTime  the current timestamp to compare against
     * @return list of due enrollment records
     */
    List<RoadmapEnrollment> findByUserIdAndNextReviewDateBefore(Long userId, LocalDateTime currentTime);

    /**
     * Finds a specific enrollment record for a student and a card.
     * Used to check if a record exists before creating a new one.
     *
     * @param userId the student's ID
     * @param cardId the milestone card's ID
     * @return Optional enrollment record
     */
    Optional<RoadmapEnrollment> findByUserIdAndCardId(Long userId, Long cardId);
}