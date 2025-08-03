package com.longrich.smartgestion.enums;

public enum TypeVente {
    AVEC_PV("Avec PV"),
    SANS_PV("Sans PV");

    private final String displayName;

    TypeVente(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}