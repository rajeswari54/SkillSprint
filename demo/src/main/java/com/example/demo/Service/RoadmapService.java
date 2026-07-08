package com.example.demo.Service;

import com.example.demo.Dto.DeckRequestDto;
import com.example.demo.Entity.LearningRoadmap;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repository.LearningRoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling all LearningRoadmap (Deck) business logic.
 */
@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final LearningRoadmapRepository roadmapRepository;

    /**
     * Fetches all available learning roadmaps.
     *
     * @return list of all LearningRoadmap entities
     */
    public List<LearningRoadmap> getAllDecks() {
        return roadmapRepository.findAll();
    }

    /**
     * Retrieves a specific roadmap by its ID.
     *
     * @param id the roadmap's primary key
     * @return the matching LearningRoadmap
     * @throws ResourceNotFoundException if no roadmap exists with the given ID
     */
    public LearningRoadmap getDeckById(Long id) {
        return roadmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + id));
    }

    /**
     * Creates a new learning roadmap from the provided request data.
     *
     * @param dto the roadmap creation payload
     * @return the persisted LearningRoadmap entity
     */
    public LearningRoadmap createDeck(DeckRequestDto dto) {
        LearningRoadmap roadmap = LearningRoadmap.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .capacity(dto.getCapacity())
                .mentorName(dto.getMentorName())
                .build();

        return roadmapRepository.save(roadmap);
    }

    /**
     * Deletes a roadmap by its ID.
     * Also implicitly removes associated milestones if cascade is configured,
     * otherwise milestone deletion should be handled here or via DB constraints.
     *
     * @param id the roadmap's primary key
     * @throws ResourceNotFoundException if no roadmap exists with the given ID
     */
    public void deleteDeck(Long id) {
        LearningRoadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + id));

        roadmapRepository.delete(roadmap);
    }
}