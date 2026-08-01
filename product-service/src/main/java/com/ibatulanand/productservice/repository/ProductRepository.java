package com.ibatulanand.productservice.repository;

import com.ibatulanand.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
