package com.longrich.smartgestion.enums;

import lombok.Getter;

@Getter
public enum StatutCommande {
    EN_COURS("EN_COURS", "En Cours"),
    LIVREE("LIVREE", "Livrée"),
    PARTIELLEMENT_LIVREE("PARTIELLEMENT_LIVREE", "Partiellement Livrée"),
    ANNULEE("ANNULEE", "Annulée"),
    EN_ATTENTE("EN_ATTENTE", "En Attente");

    private final String code;
    private final String libelle;

    StatutCommande(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }
}