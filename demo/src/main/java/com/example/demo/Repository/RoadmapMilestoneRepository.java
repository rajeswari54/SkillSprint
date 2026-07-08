package com.example.demo.Repository;

import com.example.demo.Entity.RoadmapMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RoadmapMilestone (Flashcard) entity.
 * Provides milestone lookup by parent deck.
 */
@Repository
public interface RoadmapMilestoneRepository extends JpaRepository<RoadmapMilestone, Long> {

    /**
     * Retrieves all milestones belonging to a specific roadmap (deck).
     *
     * @param deckId the parent roadmap's primary key
     * @return list of milestones for that deck
     */
    List<RoadmapMilestone> findByDeckId(Long deckId);
}