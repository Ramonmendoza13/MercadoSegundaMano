package com.mercado.mercadosegundamano.dto;

import lombok.Data;

@Data
public class CheckoutForm {
    private String street;
    private String postalCode;
    private String city;
    private String province;

    // Estos campos no se suelen guardar en BD por seguridad,
    // pero los recibimos para la "simulación"
    private String cardNumber;
    private String expiry;
    private String cvv;

    // Método de conveniencia para formatear la dirección completa
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", street, postalCode, city, province);
    }
}