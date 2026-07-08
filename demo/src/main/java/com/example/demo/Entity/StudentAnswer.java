package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity storing a student's submitted answer to a flashcard.
 * Maps to the 'student_answer' table.
 *
 * Flow:
 * 1. Student sees frontContent (question)
 * 2. Student submits their answer here
 * 3. Mentor views this answer + backContent (correct answer)
 * 4. Mentor gives score via POST /api/study/complete
 */
@Entity
@Table(name = "student_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student who submitted this answer */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The card (milestone) this answer is for */
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    /** The deck this card belongs to */
    @Column(name = "deck_id", nullable = false)
    private Long deckId;

    /** The student's actual answer text */
    @Column(nullable = false)
    private String answerText;

    /** When the student submitted the answer */
    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /**
     * Score given by mentor after evaluation (0-5).
     * NULL means not yet evaluated by mentor.
     */
    @Column
    private Integer score;

    /**
     * Status of this answer.
     * PENDING = waiting for mentor evaluation
     * EVALUATED = mentor has given a score
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerStatus status;

    public enum AnswerStatus {
        PENDING,    // Student submitted, mentor not yet graded
        EVALUATED   // Mentor has given a score
    }
}
