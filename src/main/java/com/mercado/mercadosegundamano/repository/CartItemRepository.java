package com.mercado.mercadosegundamano.repository;


import com.mercado.mercadosegundamano.model.Cart;
import com.mercado.mercadosegundamano.model.CartItem;
import com.mercado.mercadosegundamano.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {


    //comprobar si un producto ya esta en el carrito antes de volver a anadirlo
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
