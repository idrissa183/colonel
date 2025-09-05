package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lignes_entree_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneEntreeStock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entree_stock_id", nullable = false)
    private EntreeStock entreeStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @NotNull(message = "Le produit est obligatoire")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_commande_fournisseur_id")
    private LigneCommandeFournisseur ligneCommandeFournisseur;

    @Column(name = "quantite", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins de 1")
    private Integer quantite;

    @Column(name = "quantite_recue")
    @Min(value = 0, message = "La quantité reçue ne peut pas être négative")
    @Builder.Default
    private Integer quantiteRecue = 0;

    @Column(name = "prix_unitaire", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix unitaire ne peut pas être négatif")
    private BigDecimal prixUnitaire;

    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    @Column(name = "numero_lot")
    private String numeroLot;

    @Column(name = "observation")
    private String observation;

    @Column(name = "emplacement_magasin", nullable = false)
    @Builder.Default
    private String emplacementMagasin = "MAGASIN";

    public BigDecimal getMontantLigne() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }

    public BigDecimal getMontantRecu() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantiteRecue));
    }

    public boolean isComplet() {
        return quantiteRecue.equals(quantite);
    }

    public Integer getQuantiteManquante() {
        return quantite - quantiteRecue;
    }
}
