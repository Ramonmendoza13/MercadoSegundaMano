package com.mercado.mercadosegundamano.enums;

public enum HomeCondition {

    NEW("Nuevo"),
    LIKE_NEW("Como nuevo"),
    GOOD("Bueno"),
    USED("Usado");

    private final String displayName;

    HomeCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
