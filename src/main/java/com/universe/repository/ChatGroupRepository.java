package com.universe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.universe.model.ChatGroup;
import com.universe.model.User;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    // İçinde olduğum tüm grupları getir
    List<ChatGroup> findByMembersContaining(User member);
    
    // İki kişi arasındaki özel DM'i bul (Karmaşık olduğu için JPQL kullanıyoruz)
    @Query("SELECT g FROM ChatGroup g WHERE g.isPrivate = true AND :user1 MEMBER OF g.members AND :user2 MEMBER OF g.members")
    ChatGroup findPrivateChat(@Param("user1") User user1, @Param("user2") User user2);
}