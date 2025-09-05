package com.longrich.smartgestion.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransfertStockDTO {
    
    private String observation;
    private List<LigneTransfert> lignesTransfert;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LigneTransfert {
        private Long produitId;
        private Integer quantite;
        private String observation;
    }
}