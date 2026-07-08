package com.example.demo.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for LINGUIST/ADMIN to evaluate a student's submitted answer.
 * answerId = the StudentAnswer record to evaluate.
 * score    = 0 (forgot) to 5 (perfect recall).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerEvaluateDto {

    @NotNull(message = "Answer ID must not be null")
    private Long answerId;

    @NotNull(message = "Score must not be null")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;
}
