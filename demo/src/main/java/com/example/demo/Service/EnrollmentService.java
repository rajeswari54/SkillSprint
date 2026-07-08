package com.example.demo.Service;

import com.example.demo.Entity.RoadmapEnrollment;
import com.example.demo.Repository.RoadmapEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementing the SM-2 spaced-repetition algorithm
 * to manage student enrollment progress and review scheduling.
 *
 * SM-2 Algorithm Reference:
 *   EF(new) = EF(old) + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
 *   where q is the quality of recall (0–5).
 *   EF must never fall below 1.3.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final RoadmapEnrollmentRepository enrollmentRepository;

    // SM-2 constants
    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double DEFAULT_EASE_FACTOR = 2.5;

    /**
     * Updates the spaced-repetition retention record for a student and card.
     * Creates a new record if one doesn't exist.
     *
     * @param userId  the student's ID
     * @param cardId  the milestone card's ID
     * @param quality the recall quality score (0–5)
     */
    public void updateRetention(Long userId, Long cardId, int quality) {
        // Fetch existing enrollment record or create a new one
        RoadmapEnrollment enrollment = enrollmentRepository
                .findByUserIdAndCardId(userId, cardId)
                .orElseGet(() -> RoadmapEnrollment.builder()
                        .userId(userId)
                        .cardId(cardId)
                        .easeFactor(DEFAULT_EASE_FACTOR)
                        .intervalDays(1)
                        .nextReviewDate(LocalDateTime.now())
                        .build());

        // Apply SM-2 algorithm
        if (quality >= 3) {
            // Correct response: increase interval
            if (enrollment.getIntervalDays() == 1) {
                enrollment.setIntervalDays(6);
            } else {
                int newInterval = (int) Math.round(enrollment.getIntervalDays() * enrollment.getEaseFactor());
                enrollment.setIntervalDays(newInterval);
            }
        } else {
            // Incorrect response: reset interval
            enrollment.setIntervalDays(1);
        }

        // Recalculate ease factor using SM-2 formula
        double newEaseFactor = enrollment.getEaseFactor()
                + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));

        // Enforce minimum ease factor
        enrollment.setEaseFactor(Math.max(MIN_EASE_FACTOR, newEaseFactor));

        // Schedule next review date
        enrollment.setNextReviewDate(LocalDateTime.now().plusDays(enrollment.getIntervalDays()));

        enrollmentRepository.save(enrollment);
    }

    /**
     * Retrieves all milestone cards due for review for a specific student.
     * A card is due if its nextReviewDate is at or before the current time.
     *
     * @param userId the student's ID
     * @return list of due RoadmapEnrollment records
     */
    public List<RoadmapEnrollment> getDueMetrics(Long userId) {
        return enrollmentRepository.findByUserIdAndNextReviewDateBefore(userId, LocalDateTime.now());
    }
}