package com.example.demo.Controller;

import com.example.demo.Dto.DeckRequestDto;
import com.example.demo.Entity.LearningRoadmap;
import com.example.demo.Service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for LearningRoadmap (Deck) endpoints.
 * Base path: /api/decks
 */
@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    /**
     * Fetches all available learning roadmaps.
     *
     * GET /api/decks/
     *
     * @return 200 OK with list of all roadmaps
     */
    @GetMapping("/")
    public ResponseEntity<List<LearningRoadmap>> getAllDecks() {
        return ResponseEntity.ok(roadmapService.getAllDecks());
    }

    /**
     * Retrieves a specific roadmap by ID.
     *
     * GET /api/decks/{id}
     *
     * @param id the roadmap's primary key
     * @return 200 OK with the matching roadmap
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningRoadmap> getDeckById(@PathVariable Long id) {
        return ResponseEntity.ok(roadmapService.getDeckById(id));
    }

    /**
     * Creates a new learning roadmap.
     * Requires LINGUIST or ADMIN role.
     *
     * POST /api/decks/
     *
     * @param dto the roadmap creation payload
     * @return 200 OK with the created roadmap
     */
    @PostMapping("/")
    @PreAuthorize("hasAnyRole('LINGUIST', 'ADMIN')")
    public ResponseEntity<LearningRoadmap> createDeck(@Valid @RequestBody DeckRequestDto dto) {
        return ResponseEntity.ok(roadmapService.createDeck(dto));
    }

    /**
     * Deletes a roadmap by ID.
     * Requires ADMIN role.
     *
     * DELETE /api/decks/{id}
     *
     * @param id the roadmap's primary key
     * @return 200 OK with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDeck(@PathVariable Long id) {
        roadmapService.deleteDeck(id);
        return ResponseEntity.ok(Map.of("message", "Roadmap deleted successfully"));
    }
}