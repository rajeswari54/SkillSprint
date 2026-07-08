package com.example.demo.Service;

import com.example.demo.Dto.*;
import com.example.demo.Entity.RoadmapMilestone;
import com.example.demo.Entity.StudentAnswer;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repository.RoadmapMilestoneRepository;
import com.example.demo.Repository.SprintAccountRepository;
import com.example.demo.Repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing the full answer lifecycle:
 * 1. Student submits answer
 * 2. Mentor views pending answers
 * 3. Mentor evaluates and gives score
 * 4. Student views their results
 */
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final StudentAnswerRepository answerRepository;
    private final RoadmapMilestoneRepository milestoneRepository;
    private final SprintAccountRepository accountRepository;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;

    /**
     * STEP 1 — Student submits their answer.
     * Called by LEARNER after seeing the frontContent question.
     *
     * @param userId    resolved from JWT token
     * @param dto       contains cardId, deckId, answerText
     */
    @Transactional
    public StudentAnswer submitAnswer(Long userId, AnswerSubmitDto dto) {
        // Verify the card exists
        milestoneRepository.findById(dto.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Card not found with id: " + dto.getCardId()));

        StudentAnswer answer = StudentAnswer.builder()
                .userId(userId)
                .cardId(dto.getCardId())
                .deckId(dto.getDeckId())
                .answerText(dto.getAnswerText())
                .submittedAt(LocalDateTime.now())
                .status(StudentAnswer.AnswerStatus.PENDING)
                .score(null) // No score yet — waiting for mentor
                .build();

        return answerRepository.save(answer);
    }

    /**
     * STEP 2 — Mentor views all pending answers for a deck.
     * Shows frontContent, correct answer (backContent), student's answer.
     * Called by LINGUIST or ADMIN.
     *
     * @param deckId  filter by deck
     */
    public List<AnswerResponseDto> getPendingAnswers(Long deckId) {
        return answerRepository
                .findByDeckIdAndStatus(deckId, StudentAnswer.AnswerStatus.PENDING)
                .stream()
                .map(this::toAnswerResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * STEP 3 — Mentor evaluates the answer and gives a score (0-5).
     * Triggers SM-2 update automatically.
     * Called by LINGUIST or ADMIN.
     *
     * @param dto  contains answerId and score
     */
    @Transactional
    public void evaluateAnswer(AnswerEvaluateDto dto) {
        // Find the student answer record
        StudentAnswer answer = answerRepository.findById(dto.getAnswerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Answer not found with id: " + dto.getAnswerId()));

        // Update score and mark as evaluated
        answer.setScore(dto.getScore());
        answer.setStatus(StudentAnswer.AnswerStatus.EVALUATED);
        answerRepository.save(answer);

        // Trigger SM-2 spaced repetition update for this student + card
        enrollmentService.updateRetention(
                answer.getUserId(),
                answer.getCardId(),
                dto.getScore()
        );

        // Log the study session
        SessionResultDto sessionResult = SessionResultDto.builder()
                .deckId(answer.getDeckId())
                .cardId(answer.getCardId())
                .startTime(answer.getSubmittedAt())
                .endTime(LocalDateTime.now())
                .score(dto.getScore())
                .build();

        submissionService.processSessionCompletion(answer.getUserId(), sessionResult);
    }

    /**
     * STEP 4 — Student views their own answer history and scores.
     * correctAnswer revealed only after EVALUATED status.
     * Called by LEARNER.
     *
     * @param userId  resolved from JWT token
     */
    public List<StudentResultDto> getStudentResults(Long userId) {
        return answerRepository.findByUserId(userId)
                .stream()
                .map(answer -> {
                    RoadmapMilestone card = milestoneRepository
                            .findById(answer.getCardId())
                            .orElse(null);

                    return StudentResultDto.builder()
                            .answerId(answer.getId())
                            .cardId(answer.getCardId())
                            .frontContent(card != null ? card.getFrontContent() : "N/A")
                            // Reveal correct answer only after evaluation
                            .correctAnswer(
                                answer.getStatus() == StudentAnswer.AnswerStatus.EVALUATED
                                ? (card != null ? card.getBackContent() : "N/A")
                                : "Pending evaluation"
                            )
                            .studentAnswer(answer.getAnswerText())
                            .score(answer.getScore())
                            .status(answer.getStatus().name())
                            .submittedAt(answer.getSubmittedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper: converts StudentAnswer to AnswerResponseDto for mentor view.
     * Includes correct answer (backContent) for comparison.
     */
    private AnswerResponseDto toAnswerResponseDto(StudentAnswer answer) {
        RoadmapMilestone card = milestoneRepository
                .findById(answer.getCardId())
                .orElse(null);

        String studentUsername = accountRepository
                .findById(answer.getUserId())
                .map(acc -> acc.getUsername())
                .orElse("Unknown");

        return AnswerResponseDto.builder()
                .answerId(answer.getId())
                .studentUsername(studentUsername)
                .cardId(answer.getCardId())
                .deckId(answer.getDeckId())
                .frontContent(card != null ? card.getFrontContent() : "N/A")
                .correctAnswer(card != null ? card.getBackContent() : "N/A")
                .studentAnswer(answer.getAnswerText())
                .submittedAt(answer.getSubmittedAt())
                .status(answer.getStatus().name())
                .score(answer.getScore())
                .build();
    }
}
