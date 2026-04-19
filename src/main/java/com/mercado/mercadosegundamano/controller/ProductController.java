package com.mercado.mercadosegundamano.controller;

import com.mercado.mercadosegundamano.enums.*;
import com.mercado.mercadosegundamano.model.*;
import com.mercado.mercadosegundamano.repository.OrderRepository;
import com.mercado.mercadosegundamano.repository.ProductRepository;
import com.mercado.mercadosegundamano.service.EmailService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RequestMapping agrupa todas las rutas bajo /my/products
@Controller
@RequestMapping("/my/products")
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public ProductController(ProductService productService,
                             OrderService orderService,
                             ProductRepository productRepository,
                             UserRepository userRepository,
                             OrderRepository orderRepository,
                             EmailService emailService) {
        this.productService = productService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
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

        // Para cada producto vendido/enviado, buscamos su Order para obtener la direccion de envio
        // Construimos un mapa productId -> Order que Thymeleaf usara en la vista
        Map<Long, Order> productOrders = new HashMap<>();
        for (Product p : products) {
            if (p.getStatus() == ProductStatus.SOLD || p.getStatus() == ProductStatus.SHIPPED) {
                orderRepository.findFirstByProductsContaining(p)
                        .ifPresent(order -> productOrders.put(p.getId(), order));
            }
        }
        model.addAttribute("productOrders", productOrders);

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
        model.addAttribute("techTypes", TechType.values());

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
            @RequestParam(required = false) String vehicleBrand,
            @RequestParam(required = false) String vehicleModel,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) GearboxType gearboxType,
            @RequestParam(required = false) Integer doors,
            // Campos especificos de ropa
            @RequestParam(required = false) String clothingBrand,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String gender,
            // Campos especificos de hogar
            @RequestParam(required = false) HomeCondition condition,
            @RequestParam(required = false) String dimensions,
            // Campos específicos de tecnología
            @RequestParam(required = false) TechType techType,
            @RequestParam(required = false) String techBrand,
            @RequestParam(required = false) String techModel,
            @RequestParam(required = false) String storageCapacity,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String techColor,
            // Imagenes subidas desde el formulario
            // El formulario debe tener enctype="multipart/form-data"
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (title != null && title.length() > 255 || description != null && description.length() > 255) {
            redirectAttributes.addFlashAttribute("error", "El título o la descripción no pueden superar los 255 caracteres.");
            return "redirect:/my/products/new";
        }

        try {
            User currentUser = getCurrentUser(userDetails);

            // Creamos el tipo de producto correcto segun la categoria
            Product product;

            switch (category) {
                case "VEHICLE" -> {
                    VehicleProduct v = new VehicleProduct();
                    v.setBrand(vehicleBrand);
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
                    c.setBrand(clothingBrand);
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
                case "TECHNOLOGY" -> {
                    TechProduct t = new TechProduct();
                    t.setTechType(techType);
                    t.setBrand(techBrand);
                    t.setModel(techModel);
                    t.setStorageCapacity(storageCapacity);
                    t.setRam(ram);
                    t.setColor(techColor);
                    product = t;
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
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("gearboxTypes", GearboxType.values());
        model.addAttribute("homeConditions", HomeCondition.values());
        model.addAttribute("techTypes", TechType.values());

        return "products/form";
    }

    // ── POST /my/products/{id}/edit ───────────────────────────────
    // Procesa el formulario de edicion con todos los campos específicos.
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            // Campos comunes
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            // Campos específicos de vehículos
            @RequestParam(required = false) String vehicleBrand,
            @RequestParam(required = false) String vehicleModel,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) GearboxType gearboxType,
            @RequestParam(required = false) Integer doors,
            // Campos específicos de ropa
            @RequestParam(required = false) String clothingBrand,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String gender,
            // Campos específicos de hogar
            @RequestParam(required = false) HomeCondition condition,
            @RequestParam(required = false) String dimensions,
            // Campos específicos de tecnología
            @RequestParam(required = false) TechType techType,
            @RequestParam(required = false) String techBrand,
            @RequestParam(required = false) String techModel,
            @RequestParam(required = false) String storageCapacity,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String techColor,
            // Imágenes
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (title != null && title.length() > 255 || description != null && description.length() > 255) {
            redirectAttributes.addFlashAttribute("error", "El título o la descripción no pueden superar los 255 caracteres.");
            return "redirect:/my/products/" + id + "/edit";
        }

        try {
            User currentUser = getCurrentUser(userDetails);

            // Obtenemos el producto real de la BD para actualizar sus campos específicos
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Actualizamos campos comunes
            product.setTitle(title);
            product.setDescription(description);
            product.setPrice(price);

            // Actualizamos campos específicos según el tipo real del producto
            if (product instanceof VehicleProduct v) {
                if (vehicleBrand != null && !vehicleBrand.isBlank()) v.setBrand(vehicleBrand);
                if (vehicleModel != null && !vehicleModel.isBlank()) v.setModel(vehicleModel);
                if (year != null) v.setYear(year);
                if (mileage != null) v.setMileage(mileage);
                if (fuelType != null) v.setFuelType(fuelType);
                if (gearboxType != null) v.setGearboxType(gearboxType);
                if (doors != null) v.setDoors(doors);
            } else if (product instanceof ClothingProduct c) {
                if (clothingBrand != null && !clothingBrand.isBlank()) c.setBrand(clothingBrand);
                if (size != null && !size.isBlank()) c.setSize(size);
                if (color != null && !color.isBlank()) c.setColor(color);
                if (gender != null && !gender.isBlank()) c.setGender(gender);
            } else if (product instanceof HomeProduct h) {
                if (condition != null) h.setCondition(condition);
                if (dimensions != null && !dimensions.isBlank()) h.setDimensions(dimensions);
            } else if (product instanceof TechProduct t) {
                if (techType != null) t.setTechType(techType);
                if (techBrand != null && !techBrand.isBlank()) t.setBrand(techBrand);
                if (techModel != null && !techModel.isBlank()) t.setModel(techModel);
                if (storageCapacity != null && !storageCapacity.isBlank()) t.setStorageCapacity(storageCapacity);
                if (ram != null && !ram.isBlank()) t.setRam(ram);
                if (techColor != null && !techColor.isBlank()) t.setColor(techColor);
            }

            productService.updateProduct(id, product, images, currentUser);

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

    // ── POST /my/products/{id}/ship ───────────────────────────────
    // El vendedor confirma que ha enviado un producto ya vendido.
    // Cambia el estado a SHIPPED y notifica al comprador por email.
    @PostMapping("/{id}/ship")
    public String confirmShipping(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Seguridad: solo el dueno puede confirmar el envio
        if (!product.getSeller().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para realizar esta accion");
            return "redirect:/my/products";
        }

        // Solo se puede confirmar envio de productos vendidos
        if (product.getStatus() != ProductStatus.SOLD) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes confirmar el envio de productos vendidos");
            return "redirect:/my/products";
        }

        // Cambiamos el estado a SHIPPED
        product.setStatus(ProductStatus.SHIPPED);
        productRepository.save(product);

        // Buscamos el Order para obtener la direccion y notificar al comprador
        orderRepository.findFirstByProductsContaining(product)
                .ifPresent(order -> emailService.sendShippingNotificationEmail(order, product));

        redirectAttributes.addFlashAttribute("success", "Envio confirmado. El comprador ha sido notificado por email.");
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