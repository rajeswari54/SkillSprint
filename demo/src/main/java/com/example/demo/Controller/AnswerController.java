package com.example.demo.Controller;

import com.example.demo.Dto.*;
import com.example.demo.Entity.StudentAnswer;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repository.SprintAccountRepository;
import com.example.demo.Service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for student answer submission and mentor evaluation.
 *
 * LEARNER  → POST /api/answers/submit       → submit answer
 * LEARNER  → GET  /api/answers/my-results   → view own scores
 * LINGUIST → GET  /api/answers/pending/{deckId} → view pending answers
 * LINGUIST → POST /api/answers/evaluate     → give score
 */
@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final SprintAccountRepository sprintAccountRepository;

    /**
     * LEARNER submits their answer to a card.
     * POST /api/answers/submit
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<Map<String, String>> submitAnswer(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AnswerSubmitDto dto
    ) {
        Long userId = resolveUserId(principal.getUsername());
        answerService.submitAnswer(userId, dto);
        return ResponseEntity.ok(Map.of(
                "message", "Answer submitted successfully. Waiting for mentor evaluation."
        ));
    }

    /**
     * LINGUIST/ADMIN views all pending (ungraded) answers for a deck.
     * GET /api/answers/pending/{deckId}
     */
    @GetMapping("/pending/{deckId}")
    @PreAuthorize("hasAnyRole('LINGUIST', 'ADMIN')")
    public ResponseEntity<List<AnswerResponseDto>> getPendingAnswers(
            @PathVariable Long deckId
    ) {
        return ResponseEntity.ok(answerService.getPendingAnswers(deckId));
    }

    /**
     * LINGUIST/ADMIN evaluates an answer and gives a score.
     * POST /api/answers/evaluate
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('LINGUIST', 'ADMIN')")
    public ResponseEntity<Map<String, String>> evaluateAnswer(
            @Valid @RequestBody AnswerEvaluateDto dto
    ) {
        answerService.evaluateAnswer(dto);
        return ResponseEntity.ok(Map.of(
                "message", "Answer evaluated and score recorded successfully."
        ));
    }

    /**
     * LEARNER views their own answer history and scores.
     * GET /api/answers/my-results
     */
    @GetMapping("/my-results")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<StudentResultDto>> getMyResults(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = resolveUserId(principal.getUsername());
        return ResponseEntity.ok(answerService.getStudentResults(userId));
    }

    /** Resolves userId from JWT principal username */
    private Long resolveUserId(String username) {
        return sprintAccountRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username))
                .getId();
    }
}