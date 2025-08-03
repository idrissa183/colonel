package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.TypeClient;
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
public class ClientDTO {
    
    private Long id;
    
    @NotBlank(message = "Le code est obligatoire")
    private String code;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    private String province;
    private String lieuNaissance;
    private String cnib;
    private String telephone;
    
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotNull(message = "Le type de client est obligatoire")
    private TypeClient typeClient;
    
    private String adresse;
    private String localisation;
    private String codeParrain;
    private String codePlacement;
    private Integer totalPv;
    private Boolean codeDefinitif;
    private Boolean active;
    
    // Computed fields
    private String nomComplet;
    private String clientId;
    private Boolean peutDeveniPartenaire;
}