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
public class FamilleProduitDTO {

    private Long id;

    @NotBlank(message = "Le libellé famille est obligatoire")
    private String libelleFamille;

    private String description;
    private Boolean active;

    // Computed fields
    private Integer nombreProduits;
}