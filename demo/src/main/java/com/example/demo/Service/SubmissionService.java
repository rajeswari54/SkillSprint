package com.example.demo.Service;

import com.example.demo.Dto.*;
import com.example.demo.Entity.MilestoneSubmission;
import com.example.demo.Repository.MilestoneSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Processes study session completions.
 * Called by LINGUIST or ADMIN after evaluating a student's answer.
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final MilestoneSubmissionRepository submissionRepository;
    private final EnrollmentService enrollmentService;

    /**
     * @param studentId  resolved from studentUsername by the controller
     * @param result     session details including mentor-given score
     */
    @Transactional
    public void processSessionCompletion(Long studentId, SessionResultDto result) {
        // Save the study session log
        MilestoneSubmission submission = MilestoneSubmission.builder()
                .userId(studentId)
                .deckId(result.getDeckId())
                .startTime(result.getStartTime())
                .endTime(result.getEndTime())
                .score(result.getScore())
                .build();

        submissionRepository.save(submission);

        // Update Bob's SM-2 spaced repetition metrics
        enrollmentService.updateRetention(studentId, result.getCardId(), result.getScore());
    }
}