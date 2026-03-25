package com.mercado.mercadosegundamano.enums;

public enum ProductStatus {
    AVAILABLE("Disponible"),
    SOLD("Vendido");

    // Los enums pueden tener atributos y métodos como cualquier clase
    // Aquí guardamos una etiqueta en español para mostrar en la web
    private final String displayName;

    // Constructor del enum
    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    // Getter para obtener la etiqueta legible
    public String getDisplayName() {
        return displayName;
    }
}
