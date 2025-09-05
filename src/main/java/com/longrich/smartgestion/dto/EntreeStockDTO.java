package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.StatutEntreeStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntreeStockDTO {

    private Long id;
    private String numeroEntree;
    private LocalDateTime dateEntree;
    private LocalDate dateCommande;
    private LocalDate dateLivraison;
    private Long fournisseurId;
    private String nomFournisseur;
    private String numeroFactureFournisseur;
    private String numeroBonLivraison;
    private StatutEntreeStock statut;
    private BigDecimal montantTotal;
    private String observation;
    private Long utilisateurId;
    private String nomUtilisateur;
    private List<LigneEntreeStockDTO> lignesEntree;

    // Propriétés calculées
    private Integer totalQuantite;
    private Integer totalQuantiteRecue;
    private boolean complet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}