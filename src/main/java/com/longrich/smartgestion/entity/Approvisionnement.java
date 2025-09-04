package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "approvisionnements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approvisionnement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être positive")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "prix_total", precision = 10, scale = 2)
    private BigDecimal prixTotal;

    @Column(name = "date_approvisionnement", nullable = false)
    private LocalDate dateApprovisionnement;

    @Column(name = "date_reception")
    private LocalDate dateReception;

    @Column(name = "numero_commande")
    private String numeroCommande;

    @Column(name = "numero_facture")
    private String numeroFacture;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20)
    @Builder.Default
    private StatutApprovisionnement statut = StatutApprovisionnement.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @PrePersist
    private void prePersist() {
        if (dateApprovisionnement == null) {
            dateApprovisionnement = LocalDate.now();
        }
        calculerPrixTotal();
    }

    @PreUpdate
    private void preUpdate() {
        calculerPrixTotal();
    }

    private void calculerPrixTotal() {
        if (prixUnitaire != null && quantite != null) {
            prixTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

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
}