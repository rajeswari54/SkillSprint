package com.example.demo.Repository;

import com.example.demo.Entity.LearningRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for LearningRoadmap entity.
 * Provides standard CRUD operations for roadmap management.
 */
@Repository
public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, Long> {
    // Standard JpaRepository methods cover all required roadmap operations.
    // Custom queries can be added here as the project evolves.
}