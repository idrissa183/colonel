package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.TypeClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    
    private Long id;
    
    @NotBlank(message = "Le code est obligatoire")
    private String code;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    private String province;
    
    private String telephone;
    
    @Email(message = "Format d'email invalide")
    private String email;
    
    private TypeClient typeClient;
    
    private String adresse;
    
    private Boolean active;
    
    // Champs calculés
    private String nomComplet;
    private Integer nombreCommandes;
    private java.math.BigDecimal chiffreAffaires;
}
