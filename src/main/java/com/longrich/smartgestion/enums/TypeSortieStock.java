package com.longrich.smartgestion.enums;

public enum TypeSortieStock {
    VENTE("Vente"),
    PERTE("Perte"),
    TRANSFERT("Transfert"), 
    PEREMPTION("Péremption");

    private final String libelle;

    TypeSortieStock(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}