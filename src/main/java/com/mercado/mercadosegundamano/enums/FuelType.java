package com.mercado.mercadosegundamano.enums;

public enum FuelType {
    GASOLINE("Gasolina"),
    DIESEL("Diesel"),
    ELECTRIC("Electrico"),
    HYBRID("Hibrido");

    private final String displayName;

    FuelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
