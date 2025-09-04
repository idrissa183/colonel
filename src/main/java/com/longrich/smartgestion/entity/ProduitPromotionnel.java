package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "produit_promotionnel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitPromotionnel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_promotionnelle_id", nullable = false)
    private VentePromotionnelle ventePromotionnelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_bonus_id", nullable = false)
    private Produit produitBonus;

    @NotNull(message = "La quantité minimum pour le bonus est obligatoire")
    @Min(value = 1, message = "La quantité minimum doit être au moins 1")
    @Column(name = "quantite_minimum", nullable = false)
    private Integer quantiteMinimum;

    @NotNull(message = "La quantité de bonus est obligatoire")
    @Min(value = 1, message = "La quantité de bonus doit être au moins 1")
    @Column(name = "quantite_bonus", nullable = false)
    private Integer quantiteBonus;

    @Column(name = "description")
    private String description;
}