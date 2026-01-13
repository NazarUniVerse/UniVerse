package com.universe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}