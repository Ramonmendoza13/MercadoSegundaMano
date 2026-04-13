package com.mercado.mercadosegundamano.model;

import com.mercado.mercadosegundamano.enums.FuelType;
import com.mercado.mercadosegundamano.enums.GearboxType;
import com.mercado.mercadosegundamano.enums.HomeCondition;
import com.mercado.mercadosegundamano.enums.ProductCategory;
import com.mercado.mercadosegundamano.enums.ProductStatus;
import com.mercado.mercadosegundamano.enums.TechType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
// Estrategia JOINED: una tabla por clase. La tabla "products" tendrá los
// atributos comunes y cada tabla hija (vehicles, clothes...) tendrá los suyos
@Inheritance(strategy = InheritanceType.JOINED)
// Columna que distingue qué tipo de producto es cada fila
@DiscriminatorColumn(name = "category")
@Table(name = "products")

public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String title;

    @Column(nullable = false)
    @NotBlank
    private String description;

    @Column(nullable = false)
    @NotNull
    @Positive
    private BigDecimal price;

    // El enum se guarda como texto en la BD con @Enumerated
    // EnumType.STRING guarda "AVAILABLE" o "SOLD" en vez de 0 o 1
    // Siempre usa STRING, nunca ORDINAL (si reordenas el enum se rompe la BD)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    // @ElementCollection le dice a JPA que guarde esta lista en una tabla
    // separada llamada "product_images" automáticamente, sin crear una entidad Image
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    private List<String> imagePaths;

    // Relación con el vendedor
    // Muchos productos pertenecen a un usuario (el seller)
    // La columna seller_id se creará en la tabla products
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructores
    public Product() {}

    public Product(String title, String description, BigDecimal price, User seller) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.seller = seller;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public List<String> getImagePaths() { return imagePaths; }
    public User getSeller() { return seller; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    @Transient
    public ProductCategory getCategoryEnum() {
        String disc = this.getClass().getAnnotation(DiscriminatorValue.class) != null ?
                this.getClass().getAnnotation(DiscriminatorValue.class).value() :
                null;

        if (disc == null) {
            // fallback por si algo falla
            return null;
        }

        try {
            return ProductCategory.valueOf(disc);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
    public void setSeller(User seller) { this.seller = seller; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ── Getters virtuales para campos de subclases ──────────────────────────
    // @Transient evita que JPA intente mapear estas columnas en la tabla base.
    // Las subclases sobreescriben los relevantes con sus propias columnas JPA.
    // Esto permite que Thymeleaf acceda a product.brand (etc.) sin importar
    // qué tipo concreto de producto se está renderizando.

    // Campos comunes a vehículo y ropa
    @Transient public String getBrand()       { return null; }

    // Campos de vehículo
    @Transient public String        getModel()       { return null; }
    @Transient public Integer       getYear()        { return null; }
    @Transient public Integer       getMileage()     { return null; }
    @Transient public FuelType      getFuelType()    { return null; }
    @Transient public GearboxType   getGearboxType() { return null; }
    @Transient public Integer       getDoors()       { return null; }

    // Campos de ropa
    @Transient public String getSize()   { return null; }
    @Transient public String getColor()  { return null; }
    @Transient public String getGender() { return null; }

    // Campos de hogar
    @Transient public HomeCondition getCondition()  { return null; }
    @Transient public String        getDimensions() { return null; }

    // Campos de tecnología
    @Transient public TechType getTechType()        { return null; }
    @Transient public String   getStorageCapacity() { return null; }
    @Transient public String   getRam()             { return null; }
}