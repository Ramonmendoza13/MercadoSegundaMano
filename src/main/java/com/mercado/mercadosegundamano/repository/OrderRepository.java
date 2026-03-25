package com.mercado.mercadosegundamano.repository;

import com.mercado.mercadosegundamano.model.Order;
import com.mercado.mercadosegundamano.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    //historial de compras del usuario
    List<Order> findByBuyer(User buyer);

    // igual pero ordenado del mas reciente al mas antiguo
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
}
