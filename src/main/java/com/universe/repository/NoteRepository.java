package com.universe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.LectureNote;

public interface NoteRepository extends JpaRepository<LectureNote, Long> {
    
    // Tarihe göre sıralı getir
    List<LectureNote> findAllByOrderByCreatedAtDesc();
    
    // Bölüme göre filtrele
    List<LectureNote> findByDepartmentOrderByCreatedAtDesc(String department);
    
    // YENİ: Tipe göre filtrele (Ders Notu / Çıkmış Sorular)
    List<LectureNote> findByTypeOrderByCreatedAtDesc(String type);
}