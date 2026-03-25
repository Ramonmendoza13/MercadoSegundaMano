package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.enums.ProductStatus;
import com.mercado.mercadosegundamano.model.Cart;
import com.mercado.mercadosegundamano.model.CartItem;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import com.mercado.mercadosegundamano.repository.CartItemRepository;
import com.mercado.mercadosegundamano.repository.CartRepository;
import com.mercado.mercadosegundamano.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // Devuelve el carrito del usuario.
    // Si no tiene carrito todavia lo crea automaticamente.
    // Este metodo es el punto de entrada para todas las operaciones del carrito.
    public Cart getOrCreateCart(User user) {

        // Buscamos el carrito del usuario
        Optional<Cart> cartOpt = cartRepository.findByUser(user);

        // Si ya tiene carrito lo devolvemos directamente
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }

        // Si no tiene carrito creamos uno nuevo y lo guardamos
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setItems(new ArrayList<>());
        return cartRepository.save(newCart);
    }

    // Añade un producto al carrito del usuario.
    // Devuelve null si todo fue bien, o un String con el error si algo fallo.
    public String addItem(User user, Long productId) {

        // Buscamos el producto
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Seguridad: no se puede comprar un producto ya vendido
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            return "Este producto ya no esta disponible";
        }

        // Seguridad: un usuario no puede comprar sus propios productos
        if (product.getSeller().getId().equals(user.getId())) {
            return "No puedes añadir tu propio producto al carrito";
        }

        // Obtenemos o creamos el carrito
        Cart cart = getOrCreateCart(user);

        // Comprobamos si el producto ya esta en el carrito
        // para no añadirlo dos veces
        Optional<CartItem> itemExistente = cartItemRepository
                .findByCartAndProduct(cart, product);

        if (itemExistente.isPresent()) {
            return "Este producto ya esta en tu carrito";
        }

        // Creamos el nuevo item y lo guardamos
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        cartItemRepository.save(item);

        return null;
    }

    // Elimina un item del carrito.
    // Verifica que el item pertenezca al usuario antes de borrarlo.
    public void removeItem(User user, Long cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        // Seguridad: solo el dueno del carrito puede eliminar sus items
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este item");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    // Vacia completamente el carrito.
    // Se llama despues de completar una compra.
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}