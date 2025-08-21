package com.longrich.smartgestion.enums;

public enum TypeClient {
    NON_PARTENAIRE("Non Partenaire"),
    PARTENAIRE("Partenaire");

    private final String displayName;

    TypeClient(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLibelle() {
        return displayName;
    }
}