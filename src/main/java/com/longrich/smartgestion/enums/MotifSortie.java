package com.longrich.smartgestion.enums;

public enum MotifSortie {
    VENTE("Vente"),
    PEREMPTION("Péremption"),
    TRANSFERT("Transfert"),
    PERTE("Perte");

    private final String displayName;

    MotifSortie(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MotifSortie fromDisplayName(String displayName) {
        for (MotifSortie motif : values()) {
            if (motif.displayName.equals(displayName)) {
                return motif;
            }
        }
        return PERTE;
    }
}
