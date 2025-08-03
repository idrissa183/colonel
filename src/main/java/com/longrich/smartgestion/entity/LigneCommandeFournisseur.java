package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande_fournisseur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommandeFournisseur extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_fournisseur_id", nullable = false)
    private CommandeFournisseur commandeFournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être positif")
    @Column(name = "prix_unitaire", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", precision = 10, scale = 2)
    private BigDecimal sousTotal;

    @PrePersist
    @PreUpdate
    public void calculerSousTotal() {
        if (quantite != null && prixUnitaire != null) {
            sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}