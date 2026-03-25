package com.mercado.mercadosegundamano.controller;

import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.mercado.mercadosegundamano.repository.ProductRepository;

import java.util.List;

@Controller
public class PublicController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    public PublicController(ProductService productService,
                            ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    // ── GET / ─────────────────────────────────────────────────────
    // Pagina principal publica.
    // Mostramos los ultimos productos disponibles como escaparate.
    @GetMapping("/")
    public String home(Model model) {

        // Obtenemos todos los productos disponibles
        List<Product> productos = productService.getAllAvailable();

        // Pasamos la lista al modelo para que Thymeleaf la muestre
        model.addAttribute("productos", productos);

        // Retornamos el nombre de la plantilla sin extension
        // Thymeleaf buscara: templates/index.html
        return "index";
    }

    // ── GET /products/{id} ────────────────────────────────────────
    // Pagina de detalle de un producto concreto.
    // @PathVariable captura el {id} de la URL.
    // Ejemplo: /products/42 → id = 42
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {

        // Buscamos el producto por id.
        // orElseThrow lanza una excepcion si no existe,
        // Spring la convierte automaticamente en un error 404.
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("product", product);

        // Thymeleaf buscara: templates/products/detail.html
        return "products/detail";
    }

    @GetMapping("/products")
    public String catalog(@RequestParam(required = false) String query,
                          @RequestParam(required = false) String category,
                          Model model) {

        List<Product> productos;

        if (query != null && !query.isBlank()) {
            productos = productService.searchProducts(query);
            model.addAttribute("query", query);
        } else if (category != null && !category.isBlank()) {
            productos = productService.getAllAvailableByCategory(category);
        } else {
            productos = productService.getAllAvailable();
        }

        model.addAttribute("productos", productos);
        return "products/list";
    }
}