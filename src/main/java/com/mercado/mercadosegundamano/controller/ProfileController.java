package com.mercado.mercadosegundamano.controller;

import com.mercado.mercadosegundamano.model.User;
import com.mercado.mercadosegundamano.repository.UserRepository;
import com.mercado.mercadosegundamano.service.ProductService;
import com.mercado.mercadosegundamano.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public ProfileController(UserService userService,
                             ProductService productService,
                             UserRepository userRepository) {
        this.userService = userService;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    // Metodo auxiliar para obtener la entidad User desde Spring Security
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ── GET /profile ──────────────────────────────────────────────
    // Muestra el perfil del usuario con sus datos y sus productos.
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {

        User currentUser = getCurrentUser(userDetails);

        // Pasamos el usuario al modelo para mostrar sus datos
        model.addAttribute("user", currentUser);

        // Pasamos tambien sus productos para mostrarlos en el perfil
        model.addAttribute("products", productService.getProductsBySeller(currentUser));

        return "profile/profile";
    }

    // ── POST /profile/avatar ──────────────────────────────────────
    // Actualiza el avatar del usuario.
    // El formulario debe tener enctype="multipart/form-data"
    @PostMapping("/avatar")
    public String updateAvatar(
            // @RequestParam recoge el archivo subido del formulario.
            // El name del input en HTML debe ser "avatar"
            @RequestParam MultipartFile avatar,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser(userDetails);

            // Verificamos que el archivo no este vacio
            if (avatar.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Debes seleccionar una imagen");
                return "redirect:/profile";
            }

            // updateAvatar borra el avatar anterior si existe
            // y guarda el nuevo en disco y en BD
            userService.updateAvatar(avatar, currentUser);

            redirectAttributes.addFlashAttribute("success",
                    "Avatar actualizado correctamente");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al subir la imagen");
        }

        return "redirect:/profile";
    }

    // ── POST /profile/edit ────────────────────────────────────────
    // Actualiza los datos del perfil del usuario.
    // Por ahora solo el username, en la Parte 2 podriamos añadir mas campos.
    @PostMapping("/edit")
    public String editProfile(
            @RequestParam String username,
            @RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(userDetails);

        // Comprobamos que el nuevo username no este ya en uso
        // por otro usuario diferente al actual
        if (!currentUser.getUsername().equals(username) &&
                userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error",
                    "El nombre de usuario ya esta en uso");
            return "redirect:/profile";
        }

        // Comprobamos que el nuevo email no este ya en uso
        if (!currentUser.getEmail().equals(email) &&
                userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error",
                    "El email ya esta registrado");
            return "redirect:/profile";
        }

        // Actualizamos los datos y guardamos
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        userRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("success",
                "Perfil actualizado correctamente");

        return "redirect:/profile";
    }
}