package com.longrich.smartgestion.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitPromotionnelDTO {
    
    private Long id;
    private Long ventePromotionnelleId;
    private Long produitId;
    private String produitNom;
    private Long produitBonusId;
    private String produitBonusNom;
    private Integer quantiteMinimum;
    private Integer quantiteBonus;
    private String description;
}