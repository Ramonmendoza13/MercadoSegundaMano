package com.mercado.mercadosegundamano.enums;

public enum ProductCategory {

    CLOTHING("Ropa"),
    VEHICLE("Vehiculos"),
    HOME("Hogar"),
    TECHNOLOGY("Tecnología");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
