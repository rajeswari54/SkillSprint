package com.example.demo.Dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO returned to LEARNER showing their own answer history and scores.
 * correctAnswer (backContent) is revealed AFTER evaluation only.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResultDto {

    private Long answerId;
    private Long cardId;
    private String frontContent;

    /** Student's submitted answer */
    private String studentAnswer;

    /**
     * Correct answer revealed only after mentor has evaluated.
     * Shows null if still PENDING.
     */
    private String correctAnswer;

    private Integer score;
    private String status;
    private LocalDateTime submittedAt;
}