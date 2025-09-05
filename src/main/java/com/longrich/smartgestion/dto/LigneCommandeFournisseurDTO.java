package com.longrich.smartgestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommandeFournisseurDTO {
    private Long id;
    private Long commandeFournisseurId;
    private Long produitId;
    private String produitLibelle;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
}