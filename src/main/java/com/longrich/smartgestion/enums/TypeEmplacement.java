package com.longrich.smartgestion.enums;

public enum TypeEmplacement {
    MAGASIN("Magasin"),
    SURFACE_VENTE("Surface de Vente"),
    ENTREPOT("Entrepôt"),
    TRANSIT("Transit");

    private final String displayName;

    TypeEmplacement(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}