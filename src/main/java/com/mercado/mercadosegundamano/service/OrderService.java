package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.enums.ProductStatus;
import com.mercado.mercadosegundamano.model.Cart;
import com.mercado.mercadosegundamano.model.CartItem;
import com.mercado.mercadosegundamano.model.Order;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import com.mercado.mercadosegundamano.repository.OrderRepository;
import com.mercado.mercadosegundamano.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
    }

    // Procesa la compra completa.
    // @Transactional es CRITICO aqui: si algo falla a mitad (por ejemplo
    // al guardar el Order) Hibernate deshace TODOS los cambios anteriores.
    // Ningun producto quedara marcado como SOLD si la compra no se completa.
    @Transactional
    public Order checkout(User user, String shippingAddress) {

        // Obtenemos el carrito del usuario
        Cart cart = cartService.getOrCreateCart(user);

        // Verificamos que el carrito no este vacio
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito esta vacio");
        }

        // Recogemos todos los productos del carrito en una lista
        List<Product> productosComprados = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            productosComprados.add(item.getProduct());
        }

        // Creamos el pedido con todos los datos
        Order order = new Order();
        order.setBuyer(user);
        order.setShippingAddress(shippingAddress);
        order.setProducts(productosComprados);

        // Calculamos el total sumando los precios de todos los productos
        // cart.getTotal() ya hace este calculo iterando los items
        order.setTotalPrice(cart.getTotal());

        // Guardamos el pedido en la BD
        orderRepository.save(order);

        // Marcamos cada producto como SOLD para que no se pueda volver a comprar
        for (Product product : productosComprados) {
            product.setStatus(ProductStatus.SOLD);
            productRepository.save(product);
        }

        // Vaciamos el carrito
        cartService.clearCart(cart);

        return order;
    }

    // Devuelve el historial de compras de un usuario
    // ordenado del mas reciente al mas antiguo
    public List<Order> getOrderHistory(User user) {
        return orderRepository.findByBuyerOrderByCreatedAtDesc(user);
    }
}