package com.longrich.smartgestion.enums;

public enum TypeMouvement {
    ENTREE("Entrée de Stock"),
    SORTIE("Sortie de Stock"),
    ENTREPOT_VERS_MAGASIN("Entrepôt vers Magasin"),
    MAGASIN_VERS_SALLE_VENTE("Magasin vers Salle de Vente"),
    SALLE_VENTE_VERS_CLIENT("Salle de Vente vers Client"),
    RETOUR_CLIENT("Retour Client"),
    AJUSTEMENT_INVENTAIRE("Ajustement Inventaire");

    private final String displayName;

    TypeMouvement(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}