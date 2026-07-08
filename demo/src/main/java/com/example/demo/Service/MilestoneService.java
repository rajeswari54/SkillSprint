package com.example.demo.Service;

import com.example.demo.Entity.LearningRoadmap;
import com.example.demo.Entity.RoadmapMilestone;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repository.LearningRoadmapRepository;
import com.example.demo.Repository.RoadmapMilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final RoadmapMilestoneRepository milestoneRepository;
    private final LearningRoadmapRepository roadmapRepository;

    public List<RoadmapMilestone> getCardsByDeck(Long deckId) {
        roadmapRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + deckId));

        return milestoneRepository.findByDeckId(deckId);
    }

    public RoadmapMilestone addCardToDeck(Long deckId, RoadmapMilestone card) {
        LearningRoadmap deck = roadmapRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + deckId));

        card.setDeck(deck);
        return milestoneRepository.save(card);
    }
}