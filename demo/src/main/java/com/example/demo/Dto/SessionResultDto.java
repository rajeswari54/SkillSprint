package com.example.demo.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for submitting a completed study session.
 * Only LINGUIST or ADMIN can submit this.
 * studentUsername identifies whose progress is being updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResultDto {

    /** The student whose performance is being recorded. */
    @NotBlank(message = "Student username must not be blank")
    private String studentUsername;

    @NotNull(message = "Deck ID must not be null")
    private Long deckId;

    @NotNull(message = "Card ID must not be null")
    private Long cardId;

    @NotNull(message = "Start time must not be null")
    private LocalDateTime startTime;

    @NotNull(message = "End time must not be null")
    private LocalDateTime endTime;

    /**
     * Score given BY THE MENTOR after evaluating student answer.
     * 0 = complete blackout, 5 = perfect recall.
     */
    @NotNull(message = "Score must not be null")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;
}