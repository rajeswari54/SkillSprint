package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a single curriculum step (flashcard) within a roadmap.
 * Maps to the 'flashcard' table.
 */
@Entity
@Table(name = "flashcard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The roadmap this milestone belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private LearningRoadmap deck;

    /** The question / front side of the milestone card. */
    @Column(nullable = false)
    private String frontContent;

    /** The answer / back side of the milestone card. */
    @Column(nullable = false)
    private String backContent;
}
