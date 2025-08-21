package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.TypeClient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "^(BF\\d{8})?$", message = "Le code partenaire doit respecter le format BF suivi de 8 chiffres")
    private String codePartenaire;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    private String province;
    private String cnib;
    private String telephone;

    @Email(message = "Format d'email invalide")
    private String email;

    @NotNull(message = "Le type de client est obligatoire")
    private TypeClient typeClient;

    private String adresse;
    private Integer totalPv;
    private Boolean codeDefinitif;
    private Boolean active;

    // Computed fields
    private String nomComplet;
    private String clientId;
    private Boolean peutDeveniPartenaire;
    private Boolean estPartenaire;
}