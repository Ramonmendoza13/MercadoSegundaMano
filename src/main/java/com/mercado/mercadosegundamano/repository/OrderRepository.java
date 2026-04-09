package com.mercado.mercadosegundamano.repository;

import com.mercado.mercadosegundamano.model.Order;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    //historial de compras del usuario
    List<Order> findByBuyer(User buyer);

    // igual pero ordenado del mas reciente al mas antiguo
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    // Encuentra el pedido que contiene un producto concreto.
    // Usamos findFirst para evitar NonUniqueResultException en caso de datos duplicados en BD.
    Optional<Order> findFirstByProductsContaining(Product product);
}
