package com.mercado.mercadosegundamano.service;

import com.mercado.mercadosegundamano.model.Order;
import com.mercado.mercadosegundamano.model.Product;
import com.mercado.mercadosegundamano.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Async
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

    // ── Email al COMPRADOR ─────────────────────────────────────────────────────
    // Confirma su compra con el detalle de productos, precio total y direccion.
    // Usa MimeMessage para enviar HTML con estilos visuales.
    @Async
    public void sendPurchaseConfirmationEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (permite adjuntos), "UTF-8" para acentos
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            User buyer = order.getBuyer();
            helper.setTo(buyer.getEmail());
            helper.setSubject("✅ Confirmación de tu compra #" + order.getId() + " - Mercado Segunda Mano");

            // Construimos las filas de la tabla de productos
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
                    
                              <!-- INFO ADICIONAL -->
                              <tr>
                                <td style="background: #fffbf0; border: 1px solid #ffe08a; border-radius: 8px; margin: 0 40px; padding: 16px 24px;">
                                  <p style="margin: 0; font-size: 13px; color: #7a5c00;">
                                    ⏳ <strong>¿Cuándo llega?</strong> El plazo de envío depende del vendedor. Puedes ver el estado de tus pedidos en
                                    <a href="http://localhost:8080/my/products/orders" style="color: #0f3460; font-weight: 600;">Mi historial de pedidos</a>.
                                  </p>
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

            helper.setText(html, true); // true = es HTML
            mailSender.send(message);

        } catch (MessagingException e) {
            // Logamos el error pero no interrumpimos la transaccion
            System.err.println("[EmailService] Error al enviar email al comprador: " + e.getMessage());
        }
    }

    // ── Emails al VENDEDOR ─────────────────────────────────────────────────────
    // Por cada vendedor distinto en el pedido, le manda un email con sus productos
    // vendidos, el comprador y la direccion a la que debe enviarlo.
    @Async
    public void sendSaleNotificationEmails(Order order) {

        // Agrupamos los productos por vendedor para un email por vendedor
        Map<User, List<Product>> productosPorVendedor = order.getProducts().stream()
                .collect(Collectors.groupingBy(Product::getSeller));

        for (Map.Entry<User, List<Product>> entry : productosPorVendedor.entrySet()) {
            User seller = entry.getKey();
            List<Product> productosVendidos = entry.getValue();
            enviarEmailVendedor(seller, productosVendidos, order);
        }
    }

    // Metodo privado auxiliar: construye y envia el email a un vendedor concreto.
    private void enviarEmailVendedor(User seller, List<Product> productosVendidos, Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            User buyer = order.getBuyer();
            helper.setTo(seller.getEmail());
            helper.setSubject("🎉 ¡Has realizado una venta! - Mercado Segunda Mano");

            // Filas de productos vendidos por este vendedor
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
                    
                              <!-- AVISO -->
                              <tr>
                                <td style="background: #fffbf0; border: 1px solid #ffe08a; border-radius: 8px; margin: 0 40px; padding: 16px 24px;">
                                  <p style="margin: 0; font-size: 13px; color: #7a5c00;">
                                    📬 <strong>Importante:</strong> Recuerda embalar bien el producto y usar un servicio de mensajería con número de seguimiento para proteger tanto al comprador como a ti.
                                  </p>
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

        } catch (MessagingException e) {
            System.err.println("[EmailService] Error al enviar email al vendedor " + seller.getEmail() + ": " + e.getMessage());
        }
    }
    // ── Email al COMPRADOR: producto enviado ──────────────────────────────────
    // Notifica al comprador que el vendedor ha confirmado el envío de su producto.
    @Async
    public void sendShippingNotificationEmail(Order order, Product product) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            User buyer = order.getBuyer();
            helper.setTo(buyer.getEmail());
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

        } catch (MessagingException e) {
            System.err.println("[EmailService] Error al enviar email de envío al comprador: " + e.getMessage());
        }
    }
}