package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.enums.ProductStatus;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import com.mercado.mercadosegundamano.repository.ProductRepository;
import jakarta.persistence.DiscriminatorValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final int maxImagesPerProduct;

    public ProductService(ProductRepository productRepository,
                          ImageService imageService,
                          @Value("${app.products.max-images:8}") int maxImagesPerProduct) {
        this.productRepository = productRepository;
        this.imageService = imageService;
        this.maxImagesPerProduct = maxImagesPerProduct;
    }

    // Crea un producto nuevo con sus imagenes.
    // Recibe el producto ya construido (Vehicle, Clothing o Home),
    // la lista de archivos subidos y el usuario vendedor.
    public Product createProduct(Product product,
                                 List<MultipartFile> images,
                                 User seller) throws IOException {
        List<MultipartFile> validImages = normalizeImages(images);

        // Asignamos el vendedor al producto
        product.setSeller(seller);

        // El producto empieza siempre como AVAILABLE
        product.setStatus(ProductStatus.AVAILABLE);

        // Guardamos cada imagen en disco y acumulamos sus nombres
        List<String> imagePaths = new ArrayList<>();
        for (MultipartFile image : validImages) {
            String nombreArchivo = imageService.saveImage(image);
            imagePaths.add(nombreArchivo);
        }

        product.setImagePaths(imagePaths);

        return productRepository.save(product);
    }

    // Actualiza los campos basicos de un producto existente.
    // Verifica que el usuario sea el dueno antes de permitir la edicion.
    public Product updateProduct(Long id,
                                 Product updatedData,
                                 List<MultipartFile> newImages,
                                 User currentUser) throws IOException {
        List<MultipartFile> validImages = normalizeImages(newImages);

        // Buscamos el producto o lanzamos excepcion si no existe
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Seguridad: solo el dueno puede editar su producto
        if (!product.getSeller().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para editar este producto");
        }

        // Actualizamos los campos comunes
        product.setTitle(updatedData.getTitle());
        product.setDescription(updatedData.getDescription());
        product.setPrice(updatedData.getPrice());

        // Si el usuario ha subido imagenes nuevas, borramos las anteriores
        // y guardamos las nuevas
        if (!validImages.isEmpty()) {
            // Borramos las imagenes anteriores del disco
            for (String imagePath : product.getImagePaths()) {
                imageService.deleteImage(imagePath);
            }

            // Guardamos las nuevas imagenes
            List<String> nuevasRutas = new ArrayList<>();
            for (MultipartFile image : validImages) {
                nuevasRutas.add(imageService.saveImage(image));
            }
            product.setImagePaths(nuevasRutas);
        }

        return productRepository.save(product);
    }

    // Borra un producto y todas sus imagenes del disco.
    // Verifica que el usuario sea el dueno antes de borrar.
    public void deleteProduct(Long id, User currentUser) throws IOException {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Seguridad: solo el dueno puede borrar su producto
        if (!product.getSeller().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permiso para borrar este producto");
        }

        // Borramos todas las imagenes del disco antes de borrar el producto
        for (String imagePath : product.getImagePaths()) {
            imageService.deleteImage(imagePath);
        }

        productRepository.delete(product);
    }

    // Devuelve todos los productos disponibles (catalogo publico)
    public List<Product> getAllAvailable() {
        return productRepository.findByStatus(ProductStatus.AVAILABLE);
    }

    // Devuelve los productos de un vendedor concreto
    public List<Product> getProductsBySeller(User seller) {
        return productRepository.findBySeller(seller);
    }

    // Busca productos por texto en el titulo
    public List<Product> searchProducts(String query) {
        return productRepository.findByTitleContainingIgnoreCase(query);
    }

    // Añadir esto, antes del método normalizeImages
    public List<Product> getAllAvailableByCategory(String category) {
        return productRepository.findByStatus(ProductStatus.AVAILABLE)
                .stream()
                .filter(p -> {
                    DiscriminatorValue dv = p.getClass().getAnnotation(DiscriminatorValue.class);
                    return dv != null && dv.value().equals(category);
                })
                .toList();
    }

    private List<MultipartFile> normalizeImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<MultipartFile> validImages = images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .toList();

        if (validImages.size() > maxImagesPerProduct) {
            throw new IllegalArgumentException(
                    "Solo se permiten " + maxImagesPerProduct + " imagenes por anuncio");
        }

        return validImages;
    }
}
