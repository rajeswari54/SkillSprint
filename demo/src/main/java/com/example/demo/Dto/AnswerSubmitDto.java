package com.example.demo.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for student submitting an answer to a flashcard.
 * Only the answer text is needed — userId comes from JWT token.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSubmitDto {

    @NotNull(message = "Card ID must not be null")
    private Long cardId;

    @NotNull(message = "Deck ID must not be null")
    private Long deckId;

    @NotBlank(message = "Answer must not be blank")
    private String answerText;
}