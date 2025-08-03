package com.longrich.smartgestion.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fournisseurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur extends BaseEntity {

    @NotBlank(message = "Le code stockiste est obligatoire")
    @Column(name = "code_stockiste", unique = true, nullable = false)
    private String codeStockiste;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "telephone")
    private String telephone;

    @Email(message = "Format d'email invalide")
    @Column(name = "email")
    private String email;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // Relations
    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeFournisseur> commandes;

    // Méthodes utilitaires
    public String getNomComplet() {
        return nom + " " + prenom;
    }
}