package com.universe.model;

import java.time.LocalDateTime;

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
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    
    // Yeni Eklenen: İletişim Numarası
    private String contactNumber;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    private LocalDateTime createdAt;

    public Product() {
        this.createdAt = LocalDateTime.now();
    }

    // --- HTML İÇİN GEREKLİ YARDIMCI METODLAR ---

    @Transient
    public String getImagePath() {
        if (imageUrl == null || id == null) return "/images/default-product.png";
        return "/uploads/products/" + id + "/" + imageUrl;
    }

    @Transient
    public String getWhatsappLink() {
        if (contactNumber == null || contactNumber.isEmpty()) return "#";
        // Boşlukları ve parantezleri temizle
        String cleanNumber = contactNumber.replaceAll("[^0-9]", "");
        // Başında 90 yoksa ekle (Basit mantık)
        if (!cleanNumber.startsWith("90")) cleanNumber = "90" + cleanNumber;
        return "https://wa.me/" + cleanNumber + "?text=Merhaba, UniVerse'teki " + title + " ilanınız için yazıyorum.";
    }

    // --- GETTER & SETTER ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}