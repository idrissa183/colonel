package com.longrich.smartgestion.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneSortieStockDTO {
    
    private Long id;
    private Long sortieStockId;
    private Long produitId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private LocalDate datePeremption;
    private String numeroLot;
    private String observation;
    private String emplacementOrigine;
    private String emplacementDestination;
    private Long stockId;
}