package com.longrich.smartgestion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalleVenteDTO {
    
    private Long id;
    
    @NotBlank(message = "Le code salle est obligatoire")
    private String codeSalle;
    
    @NotBlank(message = "Le nom de la salle est obligatoire")
    private String nomSalle;
    
    private String localisation;
    private String description;
    private Boolean active;
    
    private Long responsableId;
    private String responsableNom;
    
    // Computed fields
    private Integer nombreStocks;
}