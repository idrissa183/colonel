package com.longrich.smartgestion.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentePromotionnelleDTO {
    
    private Long id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean active;
    private Set<ProduitPromotionnelDTO> produitsPromotionnels;
    
    public boolean isActive() {
        if (active == null || !active) return false;
        LocalDate now = LocalDate.now();
        return !now.isBefore(dateDebut) && !now.isAfter(dateFin);
    }
}