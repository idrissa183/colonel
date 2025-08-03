package com.longrich.smartgestion.enums;

public enum TypeClient {
    PARTENAIRE("Partenaire"),
    NON_PARTENAIRE("Non Partenaire"),
    STOCKISTE("Stockiste"),
    EN_ATTENTE_PARTENAIRE("En Attente Partenaire");

    private final String displayName;

    TypeClient(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}