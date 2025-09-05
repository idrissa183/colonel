package com.longrich.smartgestion.enums;

public enum StatutEntreeStock {
    EN_ATTENTE("En Attente"),
    COMMANDEE("Commandée"),
    PARTIELLEMENT_RECUE("Partiellement Reçue"),
    RECUE("Reçue"),
    VALIDEE("Validée"),
    ANNULEE("Annulée");

    private final String displayName;

    StatutEntreeStock(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}