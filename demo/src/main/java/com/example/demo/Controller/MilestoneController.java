package com.example.demo.Controller;

import com.example.demo.Entity.RoadmapMilestone;
import com.example.demo.Service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for RoadmapMilestone (Flashcard) endpoints.
 * Base path: /api/decks/{id}/cards
 */
@RestController
@RequestMapping("/api/decks/{id}/cards")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    /**
     * Lists all milestones for a specific roadmap.
     *
     * GET /api/decks/{id}/cards/
     *
     * @param id the roadmap's primary key
     * @return 200 OK with list of milestones
     */
    @GetMapping("/")
    public ResponseEntity<List<RoadmapMilestone>> getCards(@PathVariable Long id) {
        return ResponseEntity.ok(milestoneService.getCardsByDeck(id));
    }

    /**
     * Adds a new milestone to the specified roadmap.
     * Requires LINGUIST or ADMIN role.
     *
     * POST /api/decks/{id}/cards/
     *
     * @param id   the roadmap's primary key
     * @param card the milestone entity (frontContent, backContent)
     * @return 200 OK with the created milestone
     */
    @PostMapping("/")
    @PreAuthorize("hasAnyRole('LINGUIST', 'ADMIN')")
    public ResponseEntity<RoadmapMilestone> addCard(
            @PathVariable Long id,
            @RequestBody RoadmapMilestone card
    ) {
        return ResponseEntity.ok(milestoneService.addCardToDeck(id, card));
    }
}