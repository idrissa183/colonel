package com.longrich.smartgestion.enums;

public enum StatutApprovisionnement {
    EN_ATTENTE("En attente"),
    COMMANDE("Commandé"),
    RECU_PARTIEL("Reçu partiel"),
    RECU_COMPLET("Reçu complet"),
    ANNULE("Annulé");

    private final String displayName;

    StatutApprovisionnement(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}