package com.longrich.smartgestion.enums;

public enum TypeClient {
    NON_PARTENAIRE("Non Partenaire"),
    PARTENAIRE("Partenaire"),
    STOCKISTE("Stockiste"),
    EN_ATTENTE_PARTENAIRE("En Attente Partenaire");

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