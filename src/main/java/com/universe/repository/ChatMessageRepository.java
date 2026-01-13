package com.universe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}