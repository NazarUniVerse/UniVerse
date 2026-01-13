package com.universe.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullname;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    private String department;
    private String bio;
    private String avatar;
    private boolean isVerified = false;

    // --- ÖZELLİKLER ---
    private boolean isVip = false;       
    private boolean ghostMode = false;   
    private String profileMusic;
    
    private LocalDateTime lastSeen = LocalDateTime.now();

    // TAKİP
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_relationships",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "followed_id")
    )
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", fetch = FetchType.EAGER)
    private Set<User> followers = new HashSet<>();

    // ENGELLENEN KULLANICILAR (YENİ EKLENDİ)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_blocks",
        joinColumns = @JoinColumn(name = "blocker_id"),
        inverseJoinColumns = @JoinColumn(name = "blocked_id")
    )
    private Set<User> blockedUsers = new HashSet<>();

    // KAYDEDİLENLER
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_saved_posts",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private Set<Post> savedPosts = new HashSet<>();

    // --- GETTER VE SETTER ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isVip() { return isVip; }
    public void setVip(boolean vip) { isVip = vip; }

    public boolean isGhostMode() { return ghostMode; }
    public void setGhostMode(boolean ghostMode) { this.ghostMode = ghostMode; }

    public String getProfileMusic() { return profileMusic; }
    public void setProfileMusic(String profileMusic) { this.profileMusic = profileMusic; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public Set<User> getFollowers() { return followers; }
    public void setFollowers(Set<User> followers) { this.followers = followers; }

    public Set<User> getFollowing() { return following; }
    public void setFollowing(Set<User> following) { this.following = following; }

    public Set<User> getBlockedUsers() { return blockedUsers; }
    public void setBlockedUsers(Set<User> blockedUsers) { this.blockedUsers = blockedUsers; }

    public Set<Post> getSavedPosts() { return savedPosts; }
    public void setSavedPosts(Set<Post> savedPosts) { this.savedPosts = savedPosts; }

    // Yardımcılar
    public int getFollowersCount() { return followers == null ? 0 : followers.size(); }
    public int getFollowingCount() { return following == null ? 0 : following.size(); }

    @Transient
    public String getAvatarPath() {
        if (avatar == null || id == null) return "/images/default-user.png";
        return "/uploads/users/" + id + "/" + avatar;
    }

    @Transient
    public String getMusicPath() {
        if (profileMusic == null || id == null) return null;
        return "/uploads/users/" + id + "/" + profileMusic;
    }
    
    @Transient
    public boolean isOnline() {
        if (ghostMode) return false;
        if (lastSeen == null) return false;
        long minutes = ChronoUnit.MINUTES.between(lastSeen, LocalDateTime.now());
        return minutes < 5;
    }

    @Transient
    public String getLastSeenText() {
        if (ghostMode) return "Gizli";
        if (lastSeen == null) return "Görülmedi";
        long minutes = ChronoUnit.MINUTES.between(lastSeen, LocalDateTime.now());
        if (minutes < 5) return "Çevrimiçi";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return "Son görülme " + lastSeen.format(formatter);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}