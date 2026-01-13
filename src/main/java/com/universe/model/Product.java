package com.universe.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(length = 1000)
    private String description;
    
    private BigDecimal price;
    private String category; // Kitap, Elektronik, Ev Eşyası, Özel Ders
    private String imageUrl;
    private String contactNumber; // 532...

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    // --- YARDIMCI METODLAR ---
    
    @Transient
    public String getImagePath() {
        if (imageUrl == null || id == null) return "/images/default-product.jpg";
        return "/uploads/products/" + id + "/" + imageUrl;
    }

    // WhatsApp Linki Oluşturucu
    @Transient
    public String getWhatsappLink() {
        if (contactNumber == null) return "#";
        // Boşlukları ve parantezleri temizle, sadece rakam bırak
        String cleanNumber = contactNumber.replaceAll("[^0-9]", "");
        // Eğer 90 ile başlamıyorsa ve 5 ile başlıyorsa başına 90 ekle (Türkiye)
        if (cleanNumber.startsWith("5")) {
            cleanNumber = "90" + cleanNumber;
        }
        return "https://wa.me/" + cleanNumber + "?text=Merhaba, UniVerse uygulamasındaki '" + title + "' ilanı için yazıyorum.";
    }
}