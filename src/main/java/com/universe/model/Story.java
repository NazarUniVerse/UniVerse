package com.universe.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "stories")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; 
    private LocalDateTime createdAt = LocalDateTime.now(); 

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; 

    // --- YENİ EKLENENLER: GÖRENLER VE BEĞENENLER ---
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "story_views",
        joinColumns = @JoinColumn(name = "story_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> viewers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "story_likes",
        joinColumns = @JoinColumn(name = "story_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>();

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<User> getViewers() { return viewers; }
    public void setViewers(Set<User> viewers) { this.viewers = viewers; }

    public Set<User> getLikes() { return likes; }
    public void setLikes(Set<User> likes) { this.likes = likes; }

    // --- YARDIMCI METODLAR ---
    @Transient
    public String getImagePath() {
        if (imageUrl == null || id == null) return null;
        return "/uploads/stories/" + id + "/" + imageUrl;
    }
}