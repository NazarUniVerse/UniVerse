package com.universe.repository;

import com.universe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    // YENİ EKLENEN: İsimden kullanıcı bulma
    User findByFullname(String fullname);
}