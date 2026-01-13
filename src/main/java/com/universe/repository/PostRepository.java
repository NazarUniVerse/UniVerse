package com.universe.repository;

import com.universe.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Tüm postları tarihe göre sırala
    List<Post> findAllByOrderByCreatedAtDesc();
    
    // Arama yap (İçerikte veya Yazar isminde)
    @Query("SELECT p FROM Post p WHERE p.content LIKE %?1% OR p.authorName LIKE %?1%")
    List<Post> search(String keyword);
    
    // Kategoriye göre filtrele
    List<Post> findByCategoryOrderByCreatedAtDesc(String category);

    // YENİ EKLENEN (Hatayı Çözen Satır): Yazar ismine göre postları getir
    List<Post> findByAuthorNameOrderByCreatedAtDesc(String authorName);
}