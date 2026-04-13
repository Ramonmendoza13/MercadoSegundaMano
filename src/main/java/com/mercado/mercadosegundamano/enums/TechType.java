package com.mercado.mercadosegundamano.enums;

public enum TechType {

    SMARTPHONE("Smartphone / Móvil"),
    TABLET("Tablet"),
    LAPTOP("Portátil"),
    DESKTOP("Ordenador de sobremesa"),
    SMARTWATCH("Smartwatch"),
    CAMERA("Cámara"),
    GAMING("Videojuegos / Consolas"),
    ACCESSORIES("Accesorios"),
    OTHER("Otro");

    private final String displayName;

    TechType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
