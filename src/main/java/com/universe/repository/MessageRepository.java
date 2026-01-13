package com.universe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}