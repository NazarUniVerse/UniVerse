package com.universe.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.Story;

public interface StoryRepository extends JpaRepository<Story, Long> {
    // Son 24 saatteki hikayeleri getir
    List<Story> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime time);
}