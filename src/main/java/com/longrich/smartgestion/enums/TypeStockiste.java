package com.longrich.smartgestion.enums;

public enum TypeStockiste {
    PERSONNE_PHYSIQUE("Personne Physique"),
    PERSONNE_MORALE("Personne Morale");

    private final String displayName;

    TypeStockiste(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLibelle() {
        return displayName;
    }
}