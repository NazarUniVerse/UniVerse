package com.universe.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(length = 1000)
    private String description;
    
    private String location; // Konum (Örn: "Kütüphane")
    private String imageUrl; // Kapak Resmi
    private String type;     // Konser, Atölye, Spor vb.

    private LocalDate date;
    private LocalTime time;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToMany
    @JoinTable(
        name = "event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> attendees = new HashSet<>();

    @Transient
    private boolean isAttending; // O anki kullanıcı katılıyor mu?

    public Event() {
        // Varsayılan saat ataması
        this.time = LocalTime.of(20, 0); 
    }

    // --- HTML İÇİN AKILLI LINKLER ---

    // Google Haritalar Linki Üretir
    @Transient
    public String getMapLink() {
        if (location == null || location.isEmpty()) return "#";
        // Boşlukları web formatına çevirir ve Google Maps linki yapar
        return "https://www.google.com/maps/search/?api=1&query=" + location.replace(" ", "+");
    }

    @Transient
    public String getImagePath() {
        if (imageUrl == null || id == null) return "/images/default-event.jpg";
        return "/uploads/events/" + id + "/" + imageUrl;
    }

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }
    public Set<User> getAttendees() { return attendees; }
    public void setAttendees(Set<User> attendees) { this.attendees = attendees; }
    public boolean isAttending() { return isAttending; }
    public void setAttending(boolean isAttending) { this.isAttending = isAttending; }
}