package com.longrich.smartgestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneEntreeStockDTO {

    private Long id;
    private Long entreeStockId;
    private Long produitId;
    private String nomProduit;
    private String codeProduit;
    private Integer quantite;
    private Integer quantiteRecue;
    private BigDecimal prixUnitaire;
    private LocalDate datePeremption;
    private String numeroLot;
    private String observation;
    private String emplacementMagasin;

    // Propriétés calculées
    private BigDecimal montantLigne;
    private BigDecimal montantRecu;
    private Integer quantiteManquante;
    private boolean complet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}