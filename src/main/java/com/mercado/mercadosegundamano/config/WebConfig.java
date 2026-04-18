package com.mercado.mercadosegundamano.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration indica que esta clase tiene configuracion de Spring
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cloudinary maneja ahora todas las imágenes en la nube.
        // Ya no es necesario registrar la carpeta /uploads/ local.
    }
}