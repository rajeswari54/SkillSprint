package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a learning path (Deck) created by a mentor.
 * Maps to the 'study_deck' table.
 */
@Entity
@Table(name = "study_deck")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title of the roadmap. Cannot be blank. */
    @Column(nullable = false)
    private String title;

    /** Detailed description of the learning path. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Maximum number of students allowed to enroll. */
    @Column
    private Integer capacity;

    /** Name of the mentor who owns this roadmap. Cannot be blank. */
    @Column(nullable = false)
    private String mentorName;
}
