package com.mercado.mercadosegundamano.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // JavaMailSender es el Bean de Spring que gestiona el envio de correos.
    // Spring lo configura automaticamente con los datos del application.properties
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Envia un email de bienvenida al usuario recien registrado.
    // Se llama desde UserService despues de guardar el usuario en BD.
    public void sendWelcomeEmail(String toEmail, String username) {

        // SimpleMailMessage es el objeto que representa el correo.
        // Para emails con HTML se usaria MimeMessage, pero para
        // texto plano SimpleMailMessage es suficiente.
        SimpleMailMessage message = new SimpleMailMessage();

        // Destinatario: el email del usuario recien registrado
        message.setTo(toEmail);

        // Asunto del correo
        message.setSubject("Bienvenido a Mercado Segunda Mano");

        // Cuerpo del correo en texto plano
        message.setText(
                "Hola " + username + ",\n\n" +
                        "Te damos la bienvenida a Mercado Segunda Mano.\n\n" +
                        "Ya puedes empezar a comprar y vender productos en nuestra plataforma.\n\n" +
                        "Accede a tu cuenta en: http://localhost:8080\n\n" +
                        "Un saludo,\n" +
                        "El equipo de Mercado Segunda Mano"
        );

        // Enviamos el correo
        mailSender.send(message);
    }
}