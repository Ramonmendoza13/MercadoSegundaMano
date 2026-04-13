# Mercado Segunda Mano 🛍️

**🌐 Visita el proyecto:** [https://mercadosegundamano.onrender.com/](https://mercadosegundamano.onrender.com/)

## Sobre el proyecto

Este es un proyecto personal desarrollado con el objetivo principal de aprender y dominar **Spring Boot**. 

He querido ir más allá de la creación de una simple aplicación MVC (Modelo-Vista-Controlador) básica, retándome a implementar características avanzadas que se encuentran en aplicaciones del mundo real, tales como:
- **Pasarela de pago real** integrada (Stripe).
- **Sistema de notificaciones por correo electrónico** automáticas.
- **Autenticación y autorización** seguras.

## ⚙️ Cómo funciona (Flujo y Detalles Técnicos)

La aplicación simula un marketplace completo de compra y venta de artículos de segunda mano entre usuarios.

### Flujo principal de la aplicación:
1. **Registro y Autenticación:** Los usuarios pueden crear una cuenta, iniciar sesión de forma segura y gestionar su perfil. Todo esto protegido mediante **Spring Security**.
2. **Publicación de Productos:** Un usuario autenticado puede subir artículos para vender, especificando detalles como nombre, descripción, precio, categoría y subiendo una imagen.
3. **Catálogo y Carrito:** Los usuarios pueden explorar el catálogo de productos disponibles, ver los detalles de cada uno y añadirlos a su carrito de compras.
4. **Checkout (Proceso de Pago):** Cuando un usuario decide finalizar su compra, es redirigido a una pasarela de pago segura impulsada por la API de **Stripe**.
5. **Notificaciones Automáticas:** Tras un pago exitoso:
   - El **comprador** recibe un email de confirmación con los detalles de su pedido.
   - El **vendedor** recibe un email notificándole de la venta, incluyendo los datos de envío del comprador.

### Stack Tecnológico:
- **Backend:** Java 21, Spring Boot 3.2.5
- **Seguridad:** Spring Security (con CSRF y encriptación de contraseñas)
- **Base de Datos:** H2 Database / Spring Data JPA
- **Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap 5 (Diseño 'Editorial Orgánico')
- **Servicios Externos:** 
  - Stripe API (Pagos)
  - JavaMailSender (Correos electrónicos)

## 💳 Datos de prueba para la Pasarela de Pago

El proyecto está configurado en **Modo Prueba** para la pasarela de pagos. Puedes usar las siguientes tarjetas proporcionadas por Stripe para simular diferentes situaciones durante el checkout:

| Situación | Número de tarjeta |
| :--- | :--- |
| **Pago exitoso** | `4242 4242 4242 4242` |
| **Pago rechazado** | `4000 0000 0000 0002` |
| **Requiere autenticación** | `4000 0025 0000 3155` |
| **Fondos insuficientes** | `4000 0000 0000 9995` |

> **Nota para todas las tarjetas:** 
> - **Fecha de expiración:** Cualquier fecha futura (ej. `12/28`).
> - **CVC:** Cualquier número de 3 dígitos (ej. `123`).

## 🚀 Despliegue

La aplicación se encuentra desplegada gratuitamente en **Render**. Debido a las limitaciones de los planes gratuitos (Free Tier), es posible que la aplicación tarde unos segundos en "despertar" si no ha recibido tráfico recientemente. ¡Gracias por la paciencia!
