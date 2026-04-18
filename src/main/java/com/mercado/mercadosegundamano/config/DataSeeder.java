package com.mercado.mercadosegundamano.config;

import com.mercado.mercadosegundamano.enums.*;
import com.mercado.mercadosegundamano.model.*;
import com.mercado.mercadosegundamano.repository.UserRepository;
import com.mercado.mercadosegundamano.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User user1 = new User("ramonmendoza", "ramonm828@gmail.com", passwordEncoder.encode("Badolatosa1_"));
            User user2 = new User("jesusruiz", "aureliomatamoros909@gmail.com", passwordEncoder.encode("Badolatosa1_"));

            user1 = userRepository.save(user1);
            user2 = userRepository.save(user2);

            // Vehicle Product (User 1)
            VehicleProduct seat = new VehicleProduct(
                    "Seat Ibiza",
                    "Coche en buen estado, revisiones al día.",
                    new BigDecimal("3500.00"),
                    user1,
                    "Seat",
                    "Ibiza",
                    2006,
                    240000,
                    FuelType.DIESEL,
                    GearboxType.MANUAL,
                    5);
            seat.setImagePaths(List.of("ibiza1.jpg", "ibiza2.jpg"));
            productRepository.save(seat);

            // Tech Product (User 1)
            TechProduct iphone = new TechProduct();
            iphone.setTitle("iPhone 17");
            iphone.setDescription("Impecable y sin rasguños. 100% de batería.");
            iphone.setPrice(new BigDecimal("750.00"));
            iphone.setSeller(user1);
            iphone.setTechType(TechType.SMARTPHONE);
            iphone.setBrand("Apple");
            iphone.setModel("17 Pro");
            iphone.setColor("Titanio");
            iphone.setStorageCapacity("256GB");
            iphone.setRam("8GB");
            iphone.setImagePaths(List.of("iphone17.webp", "iphone17_2.webp", "iphone17_3.webp"));
            productRepository.save(iphone);

            // Clothing Product (User 2)
            ClothingProduct betis = new ClothingProduct(
                    "Camiseta oficial Real Betis 2025/26",
                    "Camiseta oficial de la temporada, nueva y sin usar.",
                    new BigDecimal("80.00"),
                    user2,
                    "Hummel",
                    "L",
                    "Verde y Blanco",
                    "Hombre");
            betis.setImagePaths(List.of("betis1.webp", "betis2.webp", "betis3.webp"));
            productRepository.save(betis);

            // Home Product (User 2)
            HomeProduct sillon = new HomeProduct(
                    "Sillón de lectura",
                    "Sillón muy cómodo para leer y relajarse. En perfectas condiciones.",
                    new BigDecimal("95.00"),
                    user2,
                    HomeCondition.LIKE_NEW,
                    "90x90x100 cm");
            sillon.setImagePaths(List.of("SILLON.webp", "SILLON2.webp"));
            productRepository.save(sillon);

            System.out.println("Base de datos inicializada con datos de prueba (Seeders creados).");
        }
    }
}
