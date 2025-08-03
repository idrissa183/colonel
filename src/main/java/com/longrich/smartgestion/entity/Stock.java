package com.longrich.smartgestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @Builder.Default
    @Min(value = 0, message = "La quantité réservée ne peut pas être négative")
    @Column(name = "quantite_reservee", nullable = false)
    private Integer quantiteReservee = 0;

    @Column(name = "emplacement")
    private String emplacement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_vente_id")
    private SalleVente salleVente;

    @Column(name = "type_stock", length = 20)
    private String typeStock; // ENTREPOT, MAGASIN, SALLE_VENTE

    // Méthodes utilitaires
    public Integer getQuantiteDisponible() {
        return quantite - quantiteReservee;
    }

    public boolean isStockFaible() {
        return produit != null &&
                produit.getStockMinimum() != null &&
                getQuantiteDisponible() <= produit.getStockMinimum();
    }
}