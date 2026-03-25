package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// @Service indica a Spring que esta clase es un servicio gestionado por el contenedor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Inyectamos el repositorio para buscar usuarios en la BD
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring Security llama a este metodo automaticamente cuando alguien
    // intenta hacer login. Recibe el username del formulario de login.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscamos el usuario en la BD por su username
        // Si no existe lanzamos UsernameNotFoundException
        // Spring Security captura esta excepcion y redirige a /login?error
        com.mercado.mercadosegundamano.model.User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username
                ));

        // Devolvemos un UserDetails de Spring Security (no nuestra entidad User)
        // Spring Security usa este objeto para verificar la contrasena y gestionar la sesion.
        // User.withUsername() es un builder de Spring Security:
        // - username: el nombre de usuario
        // - password: la contrasena encriptada en BCrypt que esta en la BD
        // - authorities: los roles/permisos. De momento lista vacia porque no tenemos roles.
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}