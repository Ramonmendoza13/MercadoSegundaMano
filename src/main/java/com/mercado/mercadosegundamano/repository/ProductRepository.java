package com.mercado.mercadosegundamano.repository;

import com.mercado.mercadosegundamano.enums.ProductStatus;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

    // Busca productos por su estado (AVAILABLE, SOLD)
    List<Product> findByStatus(ProductStatus status);

    // Busca todos los productos de un vendedor, ordenados por fecha de creación descendente
    List<Product> findBySellerOrderByCreatedAtDesc(User seller);

    // Busca los productos de un vendedor filtrados por estado
    List<Product> findBySellerAndStatus(User seller, ProductStatus status);

    // Busca productos cuyo titulo contenga el texto (sin distinguir mayusculas)
    List<Product> findByTitleContainingIgnoreCase(String query);

}
