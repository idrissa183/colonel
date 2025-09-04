package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_attribue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusAttribue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_promotionnel_id", nullable = false)
    private ProduitPromotionnel produitPromotionnel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull(message = "La quantité de bonus est obligatoire")
    @Min(value = 1, message = "La quantité de bonus doit être au moins 1")
    @Column(name = "quantite_bonus", nullable = false)
    private Integer quantiteBonus;

    @NotNull(message = "La date d'attribution est obligatoire")
    @Column(name = "date_attribution", nullable = false)
    private LocalDateTime dateAttribution;

    @Builder.Default
    @Column(name = "distribue", nullable = false)
    private Boolean distribue = false;

    @Column(name = "date_distribution")
    private LocalDateTime dateDistribution;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @PrePersist
    private void prePersist() {
        if (dateAttribution == null) {
            dateAttribution = LocalDateTime.now();
        }
    }
}