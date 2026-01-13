package com.universe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.universe.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findByCategoryOrderByCreatedAtDesc(String category);
}