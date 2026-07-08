package com.example.demo.Dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO returned to LINGUIST/ADMIN showing student answer details.
 * Includes both the student's answer AND the correct answer (backContent)
 * so the mentor can compare and give a score.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponseDto {

    private Long answerId;
    private String studentUsername;
    private Long cardId;
    private Long deckId;

    /** The question that was asked */
    private String frontContent;

    /** The correct answer (from backContent) */
    private String correctAnswer;

    /** What the student actually wrote */
    private String studentAnswer;

    private LocalDateTime submittedAt;
    private String status;

    /** Null if not yet evaluated */
    private Integer score;
}