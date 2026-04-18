package com.mercado.mercadosegundamano.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {

    private final Cloudinary cloudinary;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String saveImage(MultipartFile file) throws IOException {
        String nombreUnico = UUID.randomUUID().toString();
        
        // Subimos el archivo a Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", nombreUnico,
                "folder", "mercado"
        ));

        // Devolvemos la URL segura proporcionada por Cloudinary
        return uploadResult.get("secure_url").toString();
    }

    public void deleteImage(String url) throws IOException {
        if (url == null || !url.contains("cloudinary")) {
            return;
        }

        try {
            // Extraer el public_id de la ruta. 
            // La estructura será: mercado/nombreUnico
            int startIndex = url.indexOf("mercado/");
            if (startIndex != -1) {
                String publicIdWithExtension = url.substring(startIndex);
                int lastDotIndex = publicIdWithExtension.lastIndexOf('.');
                String publicId = (lastDotIndex != -1) ? publicIdWithExtension.substring(0, lastDotIndex) : publicIdWithExtension;
                
                // Pedimos a Cloudinary que elimine el archivo
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Error eliminando imagen de Cloudinary: " + e.getMessage());
        }
    }
}