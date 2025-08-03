package com.longrich.smartgestion.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lignes_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommande extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

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

    @Builder.Default
    @Column(name = "quantite_livree", nullable = false)
    private Integer quantiteLivree = 0;

    // Méthodes utilitaires
    @PrePersist
    @PreUpdate
    public void calculerSousTotal() {
        if (quantite != null && prixUnitaire != null) {
            sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

    public Integer getQuantiteRestante() {
        return quantite - quantiteLivree;
    }

    public boolean isLivreCompletement() {
        return quantiteLivree.equals(quantite);
    }

    public boolean isLivrePartiellement() {
        return quantiteLivree > 0 && quantiteLivree < quantite;
    }
}