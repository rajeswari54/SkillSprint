package com.example.demo.Repository;

import com.example.demo.Entity.MilestoneSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MilestoneSubmission entity.
 * Manages study session log persistence and retrieval.
 */
@Repository
public interface MilestoneSubmissionRepository extends JpaRepository<MilestoneSubmission, Long> {

    /**
     * Retrieves all study sessions submitted by a specific student.
     *
     * @param userId the student's ID
     * @return list of that student's session logs
     */
    List<MilestoneSubmission> findByUserId(Long userId);
}