package com.longrich.smartgestion.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusAttribueDTO {
    
    private Long id;
    private Long produitPromotionnelId;
    private Long commandeId;
    private Long clientId;
    private String clientNom;
    private Integer quantiteBonus;
    private LocalDateTime dateAttribution;
    private Boolean distribue;
    private LocalDateTime dateDistribution;
    private String observation;
    private Long utilisateurId;
    private String produitBonusNom;
}