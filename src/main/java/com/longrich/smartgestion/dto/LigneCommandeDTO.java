package com.longrich.smartgestion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeDTO {
    
    private Long id;
    
    @NotNull(message = "La commande est obligatoire")
    private Long commandeId;
    
    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;
    private String produitLibelle;
    private String produitCode;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
    
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaire;
    
    private BigDecimal sousTotal;
    private Integer quantiteLivree;
    
    // Computed fields
    private Integer quantiteRestante;
    private Boolean livreCompletement;
    private Boolean livrePartiellement;
}