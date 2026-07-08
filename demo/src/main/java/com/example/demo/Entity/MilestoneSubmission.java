package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a discrete study session log submitted by a student.
 * Maps to the 'study_session' table.
 */
@Entity
@Table(name = "study_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student who completed this study session. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The roadmap deck this session was for. */
    @Column(name = "deck_id", nullable = false)
    private Long deckId;

    /** Timestamp when the study session started. */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** Timestamp when the study session ended. */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Performance score achieved in this session. */
    @Column(nullable = false)
    private Integer score;
}