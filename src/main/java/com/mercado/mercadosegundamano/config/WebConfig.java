package com.mercado.mercadosegundamano.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration indica que esta clase tiene configuracion de Spring
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Leemos la carpeta de uploads del application.properties
    // Es la misma propiedad que usa ImageService
    @Value("${app.upload.dir}")
    private String uploadDir;

    // Este metodo le dice a Spring como servir archivos estaticos
    // que no estan dentro de src/main/resources/static
    // Sin esto Spring no sabe donde buscar las imagenes subidas por los usuarios
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Cuando el navegador pida una URL que empiece por /uploads/
        // Spring buscara el archivo en la carpeta uploadDir del servidor
        // Ejemplo: /uploads/a3f8c2d1.jpg → ./uploads/a3f8c2d1.jpg en el disco
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");

        // IMPORTANTE: el prefijo "file:" le dice a Spring que es una ruta
        // del sistema de archivos, no una ruta dentro del classpath del proyecto.
        // Sin "file:" Spring buscaria dentro del JAR y no encontraria nada.
    }
}