package com.universe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.LectureNote;

public interface NoteRepository extends JpaRepository<LectureNote, Long> {
    List<LectureNote> findAllByOrderByCreatedAtDesc();
    List<LectureNote> findByDepartmentOrderByCreatedAtDesc(String department);
    List<LectureNote> findByTitleContainingIgnoreCase(String keyword);
}