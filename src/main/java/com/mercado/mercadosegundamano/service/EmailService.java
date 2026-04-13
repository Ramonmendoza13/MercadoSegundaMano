package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.model.Order;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Email remitente: debe estar verificado en SendGrid como Sender Identity.
    // Se lee de la variable de entorno MAIL_FROM (spring.mail.from en properties).
    @Value("${spring.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST: envia un email simple para verificar que SendGrid funciona.
    // Llamar desde un endpoint de administracion o test manual.
    // ─────────────────────────────────────────────────────────────────────────
    public void testEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TEST SENDGRID - Mercado Segunda Mano");
            helper.setText("Si recibes este email, SendGrid SMTP esta funcionando correctamente.", false);

            mailSender.send(message);
            System.out.println("[EmailService] TEST email enviado OK a: " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] ERROR en test email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BIENVENIDA al nuevo usuario registrado.
    // Se llama desde UserService despues de guardar el usuario en BD.
    // Recibe los datos directamente (String), sin acceder a entidades JPA.
    // ─────────────────────────────────────────────────────────────────────────
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Bienvenido a Mercado Segunda Mano");
            helper.setText(
                    "Hola " + username + ",\n\n" +
                    "Te damos la bienvenida a Mercado Segunda Mano.\n\n" +
                    "Ya puedes empezar a comprar y vender productos en nuestra plataforma.\n\n" +
                    "Accede a tu cuenta en: https://mercadosegundamano.onrender.com/\n\n" +
                    "Un saludo,\n" +
                    "El equipo de Mercado Segunda Mano",
                    false
            );

            mailSender.send(message);
            System.out.println("[EmailService] Email de bienvenida enviado OK a: " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] ERROR al enviar bienvenida a " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONFIRMACION DE COMPRA al comprador.
    // Recibe el objeto Order completo, ya cargado por el llamador.
    // NO hace consultas a BD. NO usa @Async.
    // ─────────────────────────────────────────────────────────────────────────
    public void sendPurchaseConfirmationEmail(Order order) {
        User buyer = order.getBuyer();
        String toEmail = buyer.getEmail();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("✅ Confirmación de tu compra #" + order.getId() + " - Mercado Segunda Mano");

            StringBuilder productRows = new StringBuilder();
            for (Product p : order.getProducts()) {
                productRows.append("""
                        <tr>
                            <td style="padding: 12px 16px; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #2d2d2d;">%s</td>
                            <td style="padding: 12px 16px; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #2d2d2d; text-align: right; font-weight: 600; white-space: nowrap;">%.2f €</td>
                        </tr>
                        """.formatted(p.getTitle(), p.getPrice()));
            }

            String fechaFormateada = order.getCreatedAt() != null
                    ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "—";

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                    <body style="margin:0; padding:0; background-color:#f5f5f5; font-family: 'Segoe UI', Arial, sans-serif;">

                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f5; padding: 40px 0;">
                        <tr>
                          <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%;">

                              <!-- CABECERA -->
                              <tr>
                                <td style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 50%%, #0f3460 100%%); border-radius: 16px 16px 0 0; padding: 40px; text-align: center;">
                                  <div style="font-size: 36px; margin-bottom: 8px;">🛍️</div>
                                  <h1 style="color: #ffffff; font-size: 22px; margin: 0 0 8px 0; font-weight: 700;">¡Compra realizada con éxito!</h1>
                                  <p style="color: #a0b4cc; font-size: 14px; margin: 0;">Pedido #%d &nbsp;·&nbsp; %s</p>
                                </td>
                              </tr>

                              <!-- SALUDO -->
                              <tr>
                                <td style="background: #ffffff; padding: 32px 40px 16px 40px;">
                                  <p style="font-size: 16px; color: #444; margin: 0;">
                                    Hola <strong>%s</strong>,<br><br>
                                    ¡Gracias por tu compra! Hemos confirmado tu pedido y los vendedores han sido notificados.
                                    A continuación encontrarás el resumen de lo que has adquirido.
                                  </p>
                                </td>
                              </tr>

                              <!-- TABLA DE PRODUCTOS -->
                              <tr>
                                <td style="background: #ffffff; padding: 16px 40px;">
                                  <h2 style="font-size: 16px; color: #1a1a2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">📦 Productos adquiridos</h2>
                                  <table width="100%%" cellpadding="0" cellspacing="0">
                                    <thead>
                                      <tr style="background: #f8f9fa;">
                                        <th style="padding: 10px 16px; text-align: left; font-size: 12px; color: #888; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Producto</th>
                                        <th style="padding: 10px 16px; text-align: right; font-size: 12px; color: #888; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Precio</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      %s
                                    </tbody>
                                  </table>
                                </td>
                              </tr>

                              <!-- TOTAL -->
                              <tr>
                                <td style="background: #ffffff; padding: 16px 40px 8px 40px;">
                                  <table width="100%%" cellpadding="0" cellspacing="0">
                                    <tr>
                                      <td style="padding: 16px; background: linear-gradient(135deg, #1a1a2e, #0f3460); border-radius: 10px; text-align: right;">
                                        <span style="color: #a0b4cc; font-size: 13px;">TOTAL PAGADO</span>
                                        <div style="color: #ffffff; font-size: 26px; font-weight: 700; margin-top: 4px;">%.2f €</div>
                                      </td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>

                              <!-- DIRECCION DE ENVIO -->
                              <tr>
                                <td style="background: #ffffff; padding: 16px 40px 32px 40px;">
                                  <h2 style="font-size: 16px; color: #1a1a2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">📍 Dirección de envío</h2>
                                  <div style="background: #f8f9fa; border-left: 4px solid #0f3460; border-radius: 8px; padding: 16px; font-size: 14px; color: #444; line-height: 1.6;">
                                    %s
                                  </div>
                                </td>
                              </tr>

                              <!-- PIE DE PAGINA -->
                              <tr>
                                <td style="background: #1a1a2e; border-radius: 0 0 16px 16px; padding: 24px 40px; text-align: center;">
                                  <p style="color: #a0b4cc; font-size: 12px; margin: 0 0 8px 0;">Mercado Segunda Mano · Compra y vende con confianza</p>
                                  <p style="color: #6b7c93; font-size: 11px; margin: 0;">
                                    Si tienes algún problema con tu pedido, contacta con nosotros a través de la plataforma.
                                  </p>
                                </td>
                              </tr>

                            </table>
                          </td>
                        </tr>
                      </table>

                    </body>
                    </html>
                    """.formatted(
                    order.getId(),
                    fechaFormateada,
                    buyer.getUsername(),
                    productRows.toString(),
                    order.getTotalPrice(),
                    order.getShippingAddress()
            );

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("[EmailService] Confirmacion de compra enviada OK a: " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] ERROR al enviar confirmacion de compra a " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NOTIFICACION DE VENTA a cada vendedor del pedido.
    // Agrupa los productos por vendedor y envia un email a cada uno.
    // Recibe el objeto Order completo, ya cargado por el llamador.
    // NO hace consultas a BD. NO usa @Async.
    // ─────────────────────────────────────────────────────────────────────────
    public void sendSaleNotificationEmails(Order order) {
        Map<User, List<Product>> productosPorVendedor = order.getProducts().stream()
                .collect(Collectors.groupingBy(Product::getSeller));

        for (Map.Entry<User, List<Product>> entry : productosPorVendedor.entrySet()) {
            enviarEmailVendedor(entry.getKey(), entry.getValue(), order);
        }
    }

    private void enviarEmailVendedor(User seller, List<Product> productosVendidos, Order order) {
        String toEmail = seller.getEmail();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 ¡Has realizado una venta! - Mercado Segunda Mano");

            User buyer = order.getBuyer();

            StringBuilder productRows = new StringBuilder();
            for (Product p : productosVendidos) {
                productRows.append("""
                        <tr>
                            <td style="padding: 12px 16px; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #2d2d2d;">%s</td>
                            <td style="padding: 12px 16px; border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #2d2d2d; text-align: right; font-weight: 600; white-space: nowrap;">%.2f €</td>
                        </tr>
                        """.formatted(p.getTitle(), p.getPrice()));
            }

            String fechaFormateada = order.getCreatedAt() != null
                    ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "—";

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                    <body style="margin:0; padding:0; background-color:#f5f5f5; font-family: 'Segoe UI', Arial, sans-serif;">

                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f5; padding: 40px 0;">
                        <tr>
                          <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%;">

                              <!-- CABECERA -->
                              <tr>
                                <td style="background: linear-gradient(135deg, #0a4f2e 0%%, #1a7a47 50%%, #22a05a 100%%); border-radius: 16px 16px 0 0; padding: 40px; text-align: center;">
                                  <div style="font-size: 36px; margin-bottom: 8px;">🎉</div>
                                  <h1 style="color: #ffffff; font-size: 22px; margin: 0 0 8px 0; font-weight: 700;">¡Enhorabuena! Has vendido un producto</h1>
                                  <p style="color: #a8e6c3; font-size: 14px; margin: 0;">Pedido #%d &nbsp;·&nbsp; %s</p>
                                </td>
                              </tr>

                              <!-- SALUDO -->
                              <tr>
                                <td style="background: #ffffff; padding: 32px 40px 16px 40px;">
                                  <p style="font-size: 16px; color: #444; margin: 0;">
                                    Hola <strong>%s</strong>,<br><br>
                                    ¡Buenas noticias! El usuario <strong>%s</strong> ha comprado tu(s) producto(s) a través de Mercado Segunda Mano.
                                    Por favor, envíalos lo antes posible a la dirección indicada más abajo.
                                  </p>
                                </td>
                              </tr>

                              <!-- TABLA DE PRODUCTOS VENDIDOS -->
                              <tr>
                                <td style="background: #ffffff; padding: 16px 40px;">
                                  <h2 style="font-size: 16px; color: #0a4f2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">📦 Producto(s) vendido(s)</h2>
                                  <table width="100%%" cellpadding="0" cellspacing="0">
                                    <thead>
                                      <tr style="background: #f0faf4;">
                                        <th style="padding: 10px 16px; text-align: left; font-size: 12px; color: #4a8c6a; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Producto</th>
                                        <th style="padding: 10px 16px; text-align: right; font-size: 12px; color: #4a8c6a; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Precio</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      %s
                                    </tbody>
                                  </table>
                                </td>
                              </tr>

                              <!-- DATOS DEL COMPRADOR -->
                              <tr>
                                <td style="background: #ffffff; padding: 8px 40px 16px 40px;">
                                  <h2 style="font-size: 16px; color: #0a4f2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">👤 Datos del comprador</h2>
                                  <div style="background: #f8f9fa; border-radius: 8px; padding: 16px; font-size: 14px; color: #444; line-height: 1.8;">
                                    <strong>Usuario:</strong> %s<br>
                                    <strong>Email:</strong> <a href="mailto:%s" style="color: #1a7a47;">%s</a>
                                  </div>
                                </td>
                              </tr>

                              <!-- DIRECCION DE ENVIO -->
                              <tr>
                                <td style="background: #ffffff; padding: 8px 40px 32px 40px;">
                                  <h2 style="font-size: 16px; color: #0a4f2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">📍 Dirección de envío</h2>
                                  <div style="background: #f0faf4; border-left: 4px solid #22a05a; border-radius: 8px; padding: 16px; font-size: 14px; color: #444; line-height: 1.6;">
                                    %s
                                  </div>
                                </td>
                              </tr>

                              <!-- PIE DE PAGINA -->
                              <tr>
                                <td style="background: #0a4f2e; border-radius: 0 0 16px 16px; padding: 24px 40px; text-align: center;">
                                  <p style="color: #a8e6c3; font-size: 12px; margin: 0 0 8px 0;">Mercado Segunda Mano · Compra y vende con confianza</p>
                                  <p style="color: #6ba889; font-size: 11px; margin: 0;">
                                    Si tienes alguna duda sobre este pedido, puedes contactar al comprador directamente por email.
                                  </p>
                                </td>
                              </tr>

                            </table>
                          </td>
                        </tr>
                      </table>

                    </body>
                    </html>
                    """.formatted(
                    order.getId(),
                    fechaFormateada,
                    seller.getUsername(),
                    buyer.getUsername(),
                    productRows.toString(),
                    buyer.getUsername(),
                    buyer.getEmail(),
                    buyer.getEmail(),
                    order.getShippingAddress()
            );

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("[EmailService] Notificacion de venta enviada OK a: " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] ERROR al enviar notificacion de venta a " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NOTIFICACION DE ENVIO al comprador.
    // El vendedor ha confirmado el envio de un producto concreto.
    // Recibe objetos completos ya cargados. NO usa @Async.
    // ─────────────────────────────────────────────────────────────────────────
    public void sendShippingNotificationEmail(Order order, Product product) {
        User buyer = order.getBuyer();
        String toEmail = buyer.getEmail();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🚚 Tu producto está en camino - Mercado Segunda Mano");

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                    <body style="margin:0; padding:0; background-color:#f5f5f5; font-family: 'Segoe UI', Arial, sans-serif;">

                      <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f5; padding: 40px 0;">
                        <tr>
                          <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%;">

                              <!-- CABECERA -->
                              <tr>
                                <td style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 50%%, #0f3460 100%%); border-radius: 16px 16px 0 0; padding: 40px; text-align: center;">
                                  <div style="font-size: 48px; margin-bottom: 8px;">🚚</div>
                                  <h1 style="color: #ffffff; font-size: 22px; margin: 0 0 8px 0; font-weight: 700;">¡Tu pedido está en camino!</h1>
                                  <p style="color: #a0b4cc; font-size: 14px; margin: 0;">Pedido #%d</p>
                                </td>
                              </tr>

                              <!-- SALUDO -->
                              <tr>
                                <td style="background: #ffffff; padding: 32px 40px 16px 40px;">
                                  <p style="font-size: 16px; color: #444; margin: 0;">
                                    Hola <strong>%s</strong>,<br><br>
                                    ¡Buenas noticias! El vendedor ha confirmado el envío de tu producto.
                                    Pronto lo recibirás en tu dirección.
                                  </p>
                                </td>
                              </tr>

                              <!-- PRODUCTO -->
                              <tr>
                                <td style="background: #ffffff; padding: 16px 40px;">
                                  <h2 style="font-size: 16px; color: #1a1a2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">📦 Producto enviado</h2>
                                  <div style="background: #f8f9fa; border-left: 4px solid #0f3460; border-radius: 8px; padding: 16px; font-size: 15px; color: #2d2d2d; font-weight: 600;">
                                    %s
                                    <span style="float:right; color:#0f3460;">%.2f €</span>
                                  </div>
                                </td>
                              </tr>

                              <!-- DIRECCION -->
                              <tr>
                                <td style="background: #ffffff; padding: 16px 40px 32px 40px;">
                                  <h2 style="font-size: 16px; color: #1a1a2e; margin: 0 0 12px 0; font-weight: 700; border-bottom: 2px solid #e8e8e8; padding-bottom: 8px;">📍 Dirección de entrega</h2>
                                  <div style="background: #f8f9fa; border-left: 4px solid #0f3460; border-radius: 8px; padding: 16px; font-size: 14px; color: #444; line-height: 1.6;">
                                    %s
                                  </div>
                                </td>
                              </tr>

                              <!-- PIE -->
                              <tr>
                                <td style="background: #1a1a2e; border-radius: 0 0 16px 16px; padding: 24px 40px; text-align: center;">
                                  <p style="color: #a0b4cc; font-size: 12px; margin: 0 0 8px 0;">Mercado Segunda Mano · Compra y vende con confianza</p>
                                  <p style="color: #6b7c93; font-size: 11px; margin: 0;">
                                    Si tienes algún problema con tu pedido, contacta con nosotros a través de la plataforma.
                                  </p>
                                </td>
                              </tr>

                            </table>
                          </td>
                        </tr>
                      </table>

                    </body>
                    </html>
                    """.formatted(
                    order.getId(),
                    buyer.getUsername(),
                    product.getTitle(),
                    product.getPrice(),
                    order.getShippingAddress()
            );

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("[EmailService] Notificacion de envio enviada OK a: " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] ERROR al enviar notificacion de envio a " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}