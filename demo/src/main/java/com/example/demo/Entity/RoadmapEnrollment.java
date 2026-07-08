package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity tracking a student's spaced-repetition progress on a specific milestone.
 * Maps to the 'retention_metric' table.
 */
@Entity
@Table(name = "retention_metric")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student (user) being tracked. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The specific milestone (flashcard) being tracked. */
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    /**
     * SM-2 ease factor: determines how quickly intervals grow.
     * Default recommended starting value: 2.5
     */
    @Column(nullable = false)
    private Double easeFactor;

    /** Current interval in days before next review. */
    @Column(nullable = false)
    private Integer intervalDays;

    /** Timestamp of the next scheduled review for this milestone. */
    @Column(nullable = false)
    private LocalDateTime nextReviewDate;
}