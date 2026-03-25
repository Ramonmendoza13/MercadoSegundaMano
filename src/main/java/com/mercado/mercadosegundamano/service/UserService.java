package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.dto.RegisterDto;
import com.mercado.mercadosegundamano.model.User;
import com.mercado.mercadosegundamano.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ImageService imageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
    }

    // Registra un nuevo usuario.
    // Devuelve null si todo fue bien, o un String con el mensaje de error si algo fallo.
    // El controlador usara ese String para mostrar el error en el formulario.
    public String register(RegisterDto dto) {

        // Comprobacion 1: el username ya existe en la BD
        if (userRepository.existsByUsername(dto.getUsername())) {
            return "El nombre de usuario ya esta en uso";
        }

        // Comprobacion 2: el email ya existe en la BD
        if (userRepository.existsByEmail(dto.getEmail())) {
            return "El email ya esta registrado";
        }

        // Comprobacion 3: las dos contrasenas del formulario coinciden
        // Esta validacion no se puede hacer con anotaciones porque compara
        // dos campos distintos del DTO, hay que hacerla a mano aqui
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return "Las contrasenas no coinciden";
        }

        // Todo correcto: creamos la entidad User con la contrasena encriptada
        // passwordEncoder.encode() aplica BCrypt, nunca guardamos contrasenas en texto plano
        User user = new User(
                dto.getUsername(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword())
        );

        userRepository.save(user);

        // null = sin errores = registro exitoso
        return null;
    }

    // Actualiza el avatar del usuario.
    // Si ya tenia avatar borra el anterior del disco antes de guardar el nuevo.
    // MultipartFile es el archivo subido desde el formulario HTML.
    public void updateAvatar(MultipartFile file, User user) throws IOException {

        // Si el usuario ya tiene un avatar guardado borramos el archivo del disco
        // para no acumular imagenes huerfanas que ocupen espacio
        if (user.getAvatarPath() != null) {
            imageService.deleteImage(user.getAvatarPath());
        }

        // Guardamos la nueva imagen en disco y obtenemos su nombre unico
        String nombreArchivo = imageService.saveImage(file);

        // Actualizamos el campo avatarPath de la entidad con el nuevo nombre
        user.setAvatarPath(nombreArchivo);

        // Persistimos los cambios en la BD
        userRepository.save(user);
    }
}