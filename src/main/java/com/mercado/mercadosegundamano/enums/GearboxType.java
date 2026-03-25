package com.mercado.mercadosegundamano.enums;

public enum GearboxType {
    MANUAL("Manual"),
    AUTOMATIC("Automatico");

    private final String displayName;

    GearboxType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
