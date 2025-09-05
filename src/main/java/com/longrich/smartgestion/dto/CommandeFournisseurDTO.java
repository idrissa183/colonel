package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeFournisseurDTO {
    private Long id;
    private String numeroCommande;
    private Long fournisseurId;
    private String fournisseurNom;
    private LocalDateTime dateCommande;
    private LocalDateTime dateLivraisonPrevue;
    private StatutCommande statut;
    private BigDecimal montantTotal;
    private String observations;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LigneCommandeFournisseurDTO> lignes;
}