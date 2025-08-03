package com.longrich.smartgestion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitDTO {
    
    private Long id;
    
    @NotBlank(message = "Le code barre est obligatoire")
    private String codeBarre;
    
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;
    
    private String description;
    private LocalDate datePeremption;
    
    @NotNull(message = "Le prix d'achat est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix d'achat doit être positif")
    private BigDecimal prixAchat;
    
    @NotNull(message = "Le prix de revente est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix de revente doit être positif")
    private BigDecimal prixRevente;
    
    @NotNull(message = "Le nombre de PV est obligatoire")
    @DecimalMin(value = "0.0", message = "Le nombre de PV doit être positif ou nul")
    private BigDecimal pv;
    
    private Long familleProduitId;
    private String familleProduitLibelle;
    private Integer stockMinimum;
    private Integer seuilAlerte;
    private Boolean active;
    
    // Computed fields
    private BigDecimal marge;
    private BigDecimal pourcentageMarge;
    private String productDisplay;
    private Boolean stockCritique;
    private Integer currentStock;
}