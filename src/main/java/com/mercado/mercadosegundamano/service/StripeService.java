package com.mercado.mercadosegundamano.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    // Leemos la clave secreta del application.properties
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.publishable.key}")
    private String publishableKey;

    // Crea un PaymentIntent en Stripe.
    // PaymentIntent = intención de cobro. Stripe lo gestiona de forma segura.
    // Devuelve el clientSecret que el frontend necesita para confirmar el pago.
    public String createPaymentIntent(BigDecimal amount) throws StripeException {

        // Inicializamos Stripe con nuestra clave secreta
        Stripe.apiKey = secretKey;

        // Stripe trabaja en centimos, no en euros con decimales.
        // 19.99€ → 1999 centimos
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                // Descripcion que aparece en el dashboard de Stripe
                .setDescription("Compra en Mercado Segunda Mano")
                // Metodo de pago: tarjeta
                .addPaymentMethodType("card")
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // El clientSecret se envia al frontend para confirmar el pago
        // Nunca envies la secretKey al frontend
        return paymentIntent.getClientSecret();
    }

    // Getter para la clave publica, necesaria en el frontend
    public String getPublishableKey() {
        return publishableKey;
    }
}