package com.longrich.smartgestion.enums;

public enum TypeAlerte {
    STOCK_FAIBLE("Stock faible"),
    RUPTURE_STOCK("Rupture de stock"),
    STOCK_NEGATIF("Stock négatif"),
    PEREMPTION_PROCHE("Péremption proche");

    private final String displayName;

    TypeAlerte(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}