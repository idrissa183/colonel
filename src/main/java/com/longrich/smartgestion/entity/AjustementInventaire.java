package com.longrich.smartgestion.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ajustements_inventaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjustementInventaire extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "quantite_theorique", nullable = false)
    private Integer quantiteTheorique;

    @Column(name = "quantite_physique", nullable = false)
    private Integer quantitePhysique;

    @Column(name = "ecart", nullable = false)
    private Integer ecart;

    @Enumerated(EnumType.STRING)
    @Column(name = "motif_ajustement", nullable = false, length = 30)
    private MotifAjustement motifAjustement;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_ajustement", nullable = false)
    private LocalDate dateAjustement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @Column(name = "document_justificatif")
    private String documentJustificatif;

    @Builder.Default
    @Column(name = "valide", nullable = false)
    private Boolean valide = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private User validateur;

    @PrePersist
    private void prePersist() {
        if (dateAjustement == null) {
            dateAjustement = LocalDate.now();
        }
        calculerEcart();
    }

    @PreUpdate
    private void preUpdate() {
        calculerEcart();
    }

    private void calculerEcart() {
        if (quantiteTheorique != null && quantitePhysique != null) {
            ecart = quantitePhysique - quantiteTheorique;
        }
    }

    public boolean estExcedentaire() {
        return ecart != null && ecart > 0;
    }

    public boolean estDeficitaire() {
        return ecart != null && ecart < 0;
    }

    public boolean estConforme() {
        return ecart != null && ecart == 0;
    }

    public enum MotifAjustement {
        INVENTAIRE_PHYSIQUE("Inventaire physique"),
        ERREUR_SAISIE("Erreur de saisie"),
        PERTE("Perte"),
        CASSE("Casse"),
        VOL("Vol"),
        PEREMPTION("Péremption"),
        RETOUR_CLIENT("Retour client"),
        AUTRE("Autre");

        private final String displayName;

        MotifAjustement(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}