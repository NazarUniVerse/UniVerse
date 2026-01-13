package com.universe.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@Entity
@Table(name = "chat_groups")
public class ChatGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Grup adı (DM ise boş olabilir veya karşı tarafın adı)
    private boolean isPrivate = false; // DM mi yoksa Grup mu?

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "chat_group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    // Yardımcı: Gruptaki son mesajı getir (Önizleme için)
    @Transient
    public String getLastMessage() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(messages.size() - 1).getContent();
        }
        return "Henüz mesaj yok.";
    }
    
    // Yardımcı: DM ise karşı tarafın adını ve resmini bul
    @Transient
    public User getOtherUser(User me) {
        for (User u : members) {
            if (!u.getId().equals(me.getId())) return u;
        }
        return me; // Kendisiyle konuşuyorsa
    }
}