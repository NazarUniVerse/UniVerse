package com.universe.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "lecture_notes")
public class LectureNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String department; // Bölüm (Bilgisayar Müh. vb)
    private String fileName;
    
    // YENİ: Not Tipi (Ders Notu, Çıkmış Sorular vb.)
    private String type; 

    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;

    private LocalDateTime createdAt;

    @Transient
    private String timeAgo; // "2 saat önce" yazısı için

    public LectureNote() {
        this.createdAt = LocalDateTime.now();
        this.type = "Ders Notu"; // Varsayılan
    }

    // --- HTML İÇİN YARDIMCI ---
    public String getTimeAgo() {
        if (createdAt == null) return "";
        PrettyTime p = new PrettyTime(new Locale("tr"));
        return p.format(Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()));
    }

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
}