package com.mercado.mercadosegundamano.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    // @Value lee la propiedad app.upload.dir del application.properties
    // y la inyecta aqui automaticamente.
    // En nuestro caso sera "./uploads"
    @Value("${app.upload.dir}")
    private String uploadDir;

    // Guarda una imagen en disco y devuelve el nombre del archivo
    // para que el controlador/servicio lo guarde en la BD.
    // MultipartFile es el tipo que usa Spring para representar
    // un archivo subido desde un formulario HTML.
    public String saveImage(MultipartFile file) throws IOException {

        // Paso 1: obtener la extension del archivo original
        // Si el usuario sube "foto.jpg", getFilenameExtension devuelve "jpg"
        // StringUtils es de Spring (org.springframework.util), no de Java
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        // Paso 2: generar un nombre unico para evitar colisiones
        // Si dos usuarios suben "foto.jpg" no se sobreescriben entre si
        // UUID genera algo como: "a3f8c2d1-4b5e-4f6a-8c9d-1e2f3a4b5c6d"
        // El resultado final seria: "a3f8c2d1-4b5e-4f6a-8c9d-1e2f3a4b5c6d.jpg"
        String nombreUnico = UUID.randomUUID().toString() + "." + extension;

        // Paso 3: construir la ruta completa donde se guardara el archivo
        // Paths.get(uploadDir) → convierte "./uploads" en un objeto Path
        // .resolve(nombreUnico) → añade el nombre del archivo a la ruta
        // Resultado: "./uploads/a3f8c2d1-4b5e-4f6a-8c9d-1e2f3a4b5c6d.jpg"
        Path rutaCompleta = Paths.get(uploadDir).resolve(nombreUnico);

        // Paso 4: crear la carpeta si no existe todavia
        // Esto es necesario la primera vez que se sube una imagen
        Files.createDirectories(rutaCompleta.getParent());

        // Paso 5: copiar el contenido del archivo subido a la ruta del disco
        // file.getInputStream() abre el flujo de bytes del archivo subido
        // Files.copy lo vuelca a la ruta que le indicamos
        Files.copy(file.getInputStream(), rutaCompleta);

        // Devolvemos solo el nombre del archivo (no la ruta completa)
        // porque en la BD guardaremos solo el nombre y la URL la construimos en la vista
        return nombreUnico;
    }

    // Borra una imagen del disco dado su nombre de archivo.
    // Se llama cuando se borra un producto o se cambia su imagen.
    public void deleteImage(String filename) throws IOException {

        // Construimos la ruta igual que en saveImage
        Path ruta = Paths.get(uploadDir).resolve(filename);

        // deleteIfExists borra el archivo si existe.
        // Si no existe no lanza excepcion, simplemente no hace nada.
        // Esto evita errores si por alguna razon el archivo ya no estaba.
        Files.deleteIfExists(ruta);
    }
}