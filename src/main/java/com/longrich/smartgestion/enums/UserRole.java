package com.longrich.smartgestion.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ADMIN", "Administrateur"),
    USER("USER", "Utilisateur");

    private final String code;
    private final String libelle;

    UserRole(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }
}