package com.example.demo.Controller;

import com.example.demo.Dto.SessionResultDto;
import com.example.demo.Entity.RoadmapEnrollment;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repository.SprintAccountRepository;
import com.example.demo.Service.EnrollmentService;
import com.example.demo.Service.SubmissionService;
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
 * REST controller for study session and enrollment endpoints.
 *
 * WHO DOES WHAT:
 * - GET /due        → LEARNER checks their own due cards
 * - POST /complete  → LINGUIST or ADMIN submits score for a student
 */
@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final SprintAccountRepository sprintAccountRepository;

    /**
     * GET /api/study/due
     * LEARNER checks which of their own cards are due for review today.
     * userId resolved from JWT token — student sees only their own cards.
     */
    @GetMapping("/due")
    public ResponseEntity<List<RoadmapEnrollment>> getDueCards(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = resolveUserId(principal.getUsername());
        return ResponseEntity.ok(enrollmentService.getDueMetrics(userId));
    }

    /**
     * POST /api/study/complete
     * LINGUIST or ADMIN submits a score for a specific student
     * after evaluating their answer against the card's backContent.
     *
     * The mentor sees:
     *   frontContent = question shown to student
     *   backContent  = correct answer
     *   student's attempt → mentor rates it 0-5 → submits here
     */
    @PostMapping("/complete")
    @PreAuthorize("hasAnyRole('LINGUIST', 'ADMIN')")
    public ResponseEntity<Map<String, String>> completeSession(
            @Valid @RequestBody SessionResultDto result
    ) {
        // Resolve the student's userId from their username
        Long studentId = resolveUserId(result.getStudentUsername());
        submissionService.processSessionCompletion(studentId, result);
        return ResponseEntity.ok(Map.of(
                "message", "Score recorded for student: " + result.getStudentUsername()
        ));
    }

    /**
     * Resolves userId from username via database lookup.
     */
    private Long resolveUserId(String username) {
        return sprintAccountRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username))
                .getId();
    }
}