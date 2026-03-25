package com.mercado.mercadosegundamano.enums;

public enum ProductCategory {

    CLOTHING("Ropa"),
    VEHICLE("Vehiculos"),
    HOME("Hogar");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
