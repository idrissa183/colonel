package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.TypeSortieStock;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortieStockDTO {
    
    private Long id;
    private String numeroSortie;
    private LocalDateTime dateSortie;
    private TypeSortieStock typeSortie;
    private String emplacementOrigine;
    private String emplacementDestination;
    private BigDecimal montantTotal;
    private String observation;
    private String referenceDocument;
    private Long utilisateurId;
    private Long factureId;
    
    private List<LigneSortieStockDTO> lignesSortie;
}