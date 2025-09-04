package com.longrich.smartgestion.enums;

public enum TypeInventaire {
    SURFACE_VENTE("Surface de vente"),
    MAGASIN("Magasin");

    private final String displayName;

    TypeInventaire(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TypeInventaire fromDisplayName(String displayName) {
        for (TypeInventaire type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return MAGASIN;
    }
}