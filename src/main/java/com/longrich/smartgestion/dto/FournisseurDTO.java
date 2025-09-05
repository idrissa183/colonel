package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.TypeStockiste;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurDTO {

    private Long id;

    @NotNull(message = "Le type de stockiste est obligatoire")
    private TypeStockiste typeStockiste;

    @NotBlank(message = "Le code stockiste est obligatoire")
    private String codeStockiste;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String prenom;

    private String adresse;
    private String telephone;

    @Email(message = "Format d'email invalide")
    private String email;

    private Boolean active;

    // Computed fields
    private String nomComplet;
    private Boolean prenomObligatoire;
    private Boolean estPersonnePhysique;
    private Boolean estPersonneMorale;
    
    public String getNomComplet() {
        if (nomComplet != null) {
            return nomComplet;
        }
        if (prenom != null && !prenom.trim().isEmpty()) {
            return nom + " " + prenom;
        }
        return nom;
    }
}