package com.mercado.mercadosegundamano.controller;

import com.mercado.mercadosegundamano.dto.CheckoutForm;
import com.mercado.mercadosegundamano.model.Cart;
import com.mercado.mercadosegundamano.model.User;
import com.mercado.mercadosegundamano.repository.UserRepository;
import com.mercado.mercadosegundamano.service.CartService;
import com.mercado.mercadosegundamano.service.OrderService;
import com.mercado.mercadosegundamano.service.StripeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    // Inyecta StripeService en CartController
    private final StripeService stripeService;

    public CartController(CartService cartService,
                          OrderService orderService,
                          UserRepository userRepository,
                          StripeService stripeService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
    }

    // Metodo auxiliar para obtener la entidad User desde Spring Security
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ── GET /cart ─────────────────────────────────────────────────
    // Muestra el carrito del usuario con todos sus items y el total.
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {

        User currentUser = getCurrentUser(userDetails);

        // getOrCreateCart crea el carrito si no existe todavia
        Cart cart = cartService.getOrCreateCart(currentUser);

        // Pasamos el carrito al modelo.
        // En la plantilla accedemos al total con cart.total
        // y a los items con cart.items
        model.addAttribute("cart", cart);

        return "cart/cart";
    }

    // ── POST /cart/add/{productId} ────────────────────────────────
    // Añade un producto al carrito.
    // Recibe el id del producto en la URL como PathVariable.
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        // addItem devuelve null si todo fue bien
        // o un String con el mensaje de error si algo fallo
        String error = cartService.addItem(currentUser, productId);

        if (error != null) {
            // addFlashAttribute sobrevive a la redireccion
            redirectAttributes.addFlashAttribute("error", error);
        } else {
            redirectAttributes.addFlashAttribute("success", "Producto añadido al carrito");
        }

        // Redirigimos al catalogo para que el usuario pueda seguir comprando
        return "redirect:/cart";
    }

    // ── POST /cart/remove/{itemId} ────────────────────────────────
    // Elimina un item concreto del carrito.
    // Recibe el id del CartItem (no del producto)
    @PostMapping("/cart/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        try {
            cartService.removeItem(currentUser, itemId);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Volvemos al carrito
        return "redirect:/cart";
    }



    @GetMapping("/checkout")
    public String showCheckout(@AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(userDetails);
            Cart cart = cartService.getOrCreateCart(currentUser);

            if (cart.getItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El carrito esta vacio");
                return "redirect:/cart";
            }

            // Creamos el PaymentIntent con el total del carrito
            String clientSecret = stripeService.createPaymentIntent(cart.getTotal());

            model.addAttribute("cart", cart);
            model.addAttribute("stripePublishableKey", stripeService.getPublishableKey());
            model.addAttribute("clientSecret", clientSecret);

            return "cart/checkout";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al preparar el pago");
            return "redirect:/cart";
        }
    }

    // ── POST /cart/checkout ───────────────────────────────────────
    // Procesa la compra completa.
    // Recibe la direccion de envio del formulario de checkout.
    @PostMapping("/cart/checkout")
    public String checkout(@ModelAttribute CheckoutForm checkoutForm,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        try {
            // Combinamos los campos en un solo String para la base de datos
            String fullAddress = checkoutForm.getFullAddress();

            // Llamamos al servicio con la dirección formateada
            orderService.checkout(currentUser, fullAddress);

            redirectAttributes.addFlashAttribute("success",
                    "Compra realizada correctamente. ¡Gracias por confiar en nosotros!");

            return "redirect:/my/products/orders";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    // GET /cart/checkout/success — Stripe redirige aqui tras pago exitoso
    @GetMapping("/cart/checkout/success")
    public String checkoutSuccess(@RequestParam String address,
                                  @RequestParam String payment_intent,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(userDetails);

            // Procesamos el pedido: marca productos como SOLD y vacia el carrito
            orderService.checkout(currentUser, address);

            // Enviamos email de confirmacion (si tienes EmailService configurado)
            // emailService.sendOrderConfirmationEmail(currentUser.getEmail(), ...);

            redirectAttributes.addFlashAttribute("success",
                    "Pago realizado correctamente. Pronto recibiras tu pedido.");
            return "redirect:/my/products/orders";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pedido");
            return "redirect:/cart";
        }
    }
}