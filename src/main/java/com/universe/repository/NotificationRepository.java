package com.universe.repository;

import com.universe.model.Notification;
import com.universe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Bana gelen bildirimleri tarihe göre getir
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    
    // Okunmamış bildirim sayısı (Kırmızı rozet için)
    long countByRecipientAndIsReadFalse(User recipient);
}