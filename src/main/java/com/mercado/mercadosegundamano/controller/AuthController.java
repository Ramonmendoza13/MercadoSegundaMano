package com.mercado.mercadosegundamano.controller;

import com.mercado.mercadosegundamano.dto.RegisterDto;
import com.mercado.mercadosegundamano.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ── GET /login ────────────────────────────────────────────────
    // Muestra el formulario de login.
    // Spring Security intercepta el POST automaticamente,
    // no necesitamos un metodo POST aqui.
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // ── GET /register ─────────────────────────────────────────────
    // Muestra el formulario de registro vacio.
    // Pasamos un RegisterDto vacio al modelo para que Thymeleaf
    // pueda enlazar cada campo con th:field
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    // ── POST /register ────────────────────────────────────────────
    @PostMapping("/register")
    public String register(
            // @Valid activa las validaciones del DTO (@NotBlank, @Email, @Size...)
            @Valid
            // @ModelAttribute enlaza el formulario HTML con el objeto RegisterDto
            @ModelAttribute("registerDto") RegisterDto registerDto,
            // BindingResult recoge los errores de validacion.
            // CRITICO: debe ir justo despues del objeto @Valid
            // si no Spring lanza excepcion en vez de capturar los errores
            BindingResult bindingResult,
            // RedirectAttributes permite pasar mensajes flash tras una redireccion
            RedirectAttributes redirectAttributes,
            Model model) {

        // Si hay errores de validacion (@NotBlank, @Email, @Size...)
        // volvemos al formulario con los mensajes de error campo a campo
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Llamamos al servicio. Devuelve null si todo fue bien,
        // o un String con el mensaje de error si algo fallo
        // (username duplicado, email duplicado, contrasenas no coinciden)
        String error = userService.register(registerDto);

        if (error != null) {
            // Pasamos el error al modelo para mostrarlo en el formulario
            model.addAttribute("error", error);
            return "auth/register";
        }

        // Todo correcto: redirigimos al login con mensaje de exito.
        // addFlashAttribute sobrevive a la redireccion (como ->with() en Laravel)
        redirectAttributes.addFlashAttribute("success",
                "Cuenta creada correctamente. Ya puedes iniciar sesion.");
        return "redirect:/login";
    }
}