package com.mercado.mercadosegundamano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Bean del encriptador de contrasenas.
    // Spring lo detecta automaticamente y lo usa para verificar
    // contrasenas durante el login y para encriptar en el registro.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean principal de seguridad.
    // Aqui definimos que rutas son publicas y cuales requieren login.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // ── Rutas PUBLICAS ──────────────────────────────────────
                        // Cualquiera puede acceder sin estar autenticado

                        // Pagina principal
                        .requestMatchers("/").permitAll()

                        // Catalogo publico de productos
                        .requestMatchers("/products/**").permitAll()

                        // Registro y login
                        .requestMatchers("/register", "/login").permitAll()

                        // Test Ping y mantenear web actriva evitando que render la apage
                        .requestMatchers("/ping").permitAll()

                        // Recursos estaticos del frontend
                        .requestMatchers("/css/**", "/js/**").permitAll()


                        // ── Rutas PRIVADAS ──────────────────────────────────────
                        // Cualquier otra ruta requiere estar autenticado
                        .anyRequest().authenticated()
                )

                // Configuracion del formulario de login
                .formLogin(form -> form
                        .loginPage("/login")               // nuestra pagina de login personalizada
                        .loginProcessingUrl("/login")      // URL donde se envia el formulario POST
                        .defaultSuccessUrl("/", true)      // redirigir al home tras login exitoso
                        .failureUrl("/login?error=true")   // redirigir si el login falla
                        .permitAll()
                )

                // Configuracion del logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)       // destruye la sesion
                        .deleteCookies("JSESSIONID")       // borra la cookie de sesion
                        .permitAll()
                )

                // Necesario para la consola H2 que usa iframes internamente.
                // Spring Security bloquea iframes por defecto con X-Frame-Options.
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        return http.build();
    }
}