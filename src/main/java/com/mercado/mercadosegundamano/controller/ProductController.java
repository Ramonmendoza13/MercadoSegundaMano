package com.mercado.mercadosegundamano.controller;

import com.mercado.mercadosegundamano.enums.*;
import com.mercado.mercadosegundamano.model.*;
import com.mercado.mercadosegundamano.repository.ProductRepository;
import com.mercado.mercadosegundamano.service.OrderService;
import com.mercado.mercadosegundamano.service.ProductService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.mercado.mercadosegundamano.repository.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

// @RequestMapping agrupa todas las rutas bajo /my/products
@Controller
@RequestMapping("/my/products")
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductService productService,
                             OrderService orderService,
                             ProductRepository productRepository,
                             UserRepository userRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Metodo auxiliar privado para obtener la entidad User a partir
    // del UserDetails que inyecta Spring Security.
    // Spring Security trabaja con UserDetails (interfaz), pero nosotros
    // necesitamos nuestra entidad User para pasarla a los servicios.
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ── GET /my/products ──────────────────────────────────────────
    // Lista todos los productos del usuario autenticado.
    // @AuthenticationPrincipal inyecta el UserDetails del usuario en sesion.
    @GetMapping
    public String myProducts(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {

        User currentUser = getCurrentUser(userDetails);

        // Obtenemos solo los productos de este vendedor
        List<Product> products = productService.getProductsBySeller(currentUser);
        model.addAttribute("products", products);

        return "products/my-products";
    }

    // ── GET /my/products/new ──────────────────────────────────────
    // Muestra el formulario para crear un nuevo producto.
    // Pasamos los valores de los enums al modelo para rellenar
    // los <select> del formulario con Thymeleaf.
    @GetMapping("/new")
    public String newProductForm(Model model) {

        // Pasamos los valores de cada enum para los desplegables del formulario
        // En Thymeleaf se iteran con th:each="cat : ${categories}"
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("gearboxTypes", GearboxType.values());
        model.addAttribute("homeConditions", HomeCondition.values());

        return "products/form";
    }

    // ── POST /my/products ─────────────────────────────────────────
    // Procesa el formulario de creacion de producto.
    // Segun la categoria recibida creamos un tipo de producto u otro.
    @PostMapping
    public String createProduct(
            // Categoria seleccionada en el formulario
            @RequestParam String category,
            // Campos comunes a todos los productos
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            // Campos especificos de vehiculos (opcionales segun categoria)
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String vehicleModel,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) GearboxType gearboxType,
            @RequestParam(required = false) Integer doors,
            // Campos especificos de ropa
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String gender,
            // Campos especificos de hogar
            @RequestParam(required = false) HomeCondition condition,
            @RequestParam(required = false) String dimensions,
            // Imagenes subidas desde el formulario
            // El formulario debe tener enctype="multipart/form-data"
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser(userDetails);

            // Creamos el tipo de producto correcto segun la categoria
            Product product;

            switch (category) {
                case "VEHICLE" -> {
                    VehicleProduct v = new VehicleProduct();
                    v.setBrand(brand);
                    v.setModel(vehicleModel);
                    v.setYear(year);
                    v.setMileage(mileage);
                    v.setFuelType(fuelType);
                    v.setGearboxType(gearboxType);
                    v.setDoors(doors);
                    product = v;
                }
                case "CLOTHING" -> {
                    ClothingProduct c = new ClothingProduct();
                    c.setBrand(brand);
                    c.setSize(size);
                    c.setColor(color);
                    c.setGender(gender);
                    product = c;
                }
                case "HOME" -> {
                    HomeProduct h = new HomeProduct();
                    h.setCondition(condition);
                    h.setDimensions(dimensions);
                    product = h;
                }
                default -> throw new RuntimeException("Categoria no valida");
            }

            // Asignamos los campos comunes a todos los productos
            product.setTitle(title);
            product.setDescription(description);
            product.setPrice(price);

            // Guardamos el producto con sus imagenes
            productService.createProduct(product, images, currentUser);

            redirectAttributes.addFlashAttribute("success", "Producto creado correctamente");
            return "redirect:/my/products";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir las imagenes");
            return "redirect:/my/products/new";
        }
    }

    // ── GET /my/products/{id}/edit ────────────────────────────────
    // Muestra el formulario de edicion con los datos actuales del producto.
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {

        User currentUser = getCurrentUser(userDetails);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Seguridad: solo el dueno puede editar su producto
        if (!product.getSeller().getId().equals(currentUser.getId())) {
            return "redirect:/my/products";
        }

        // Pasamos el producto y los enums al modelo
        model.addAttribute("product", product);
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("gearboxTypes", GearboxType.values());
        model.addAttribute("homeConditions", HomeCondition.values());

        return "products/form";
    }

    // ── POST /my/products/{id}/edit ───────────────────────────────
    // Procesa el formulario de edicion.
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser(userDetails);

            // Creamos un producto temporal solo con los campos editables
            // El servicio se encarga de actualizar solo lo necesario
            Product updatedData = new VehicleProduct();
            updatedData.setTitle(title);
            updatedData.setDescription(description);
            updatedData.setPrice(price);

            productService.updateProduct(id, updatedData, images, currentUser);

            redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir las imagenes");
        }

        return "redirect:/my/products";
    }

    // ── POST /my/products/{id}/delete ─────────────────────────────
    // Borra el producto y todas sus imagenes.
    // Usamos POST en vez de DELETE porque los formularios HTML
    // solo soportan GET y POST de forma nativa.
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(userDetails);
            productService.deleteProduct(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto");
        }

        return "redirect:/my/products";
    }

    // ── GET /my/orders ────────────────────────────────────────────
    // Historial de pedidos del usuario autenticado.
    // Aunque tecnicamente podria ir en un OrderController separado,
    // lo ponemos aqui porque es parte del area privada del usuario.
    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {

        User currentUser = getCurrentUser(userDetails);

        // Obtenemos los pedidos ordenados del mas reciente al mas antiguo
        model.addAttribute("orders", orderService.getOrderHistory(currentUser));

        return "orders/history";
    }
}