package com.universe.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    // Tarihi geçmemiş etkinlikleri getir, tarihe göre sırala
    List<Event> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);
}