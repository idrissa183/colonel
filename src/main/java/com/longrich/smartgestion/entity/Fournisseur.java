package com.longrich.smartgestion.entity;

import java.util.List;

import com.longrich.smartgestion.enums.TypeStockiste;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_stockiste", nullable = false)
    private TypeStockiste typeStockiste;

    @NotBlank(message = "Le code stockiste est obligatoire")
    @Pattern(regexp = "^[A-Z]{2}\\d{4}$", message = "Le code stockiste doit respecter le format ISO2 suivi de 4 chiffres (ex: BF1234)")
    @Column(name = "code_stockiste", unique = true, nullable = false)
    private String codeStockiste;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "adresse")
    private String adresse;

    @Pattern(regexp = "^(\\+226[02567]\\d{7}|[02567]\\d{7})?$", message = "Numéro de téléphone burkinabè invalide")
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
        if (typeStockiste == TypeStockiste.PERSONNE_PHYSIQUE && prenom != null) {
            return nom + " " + prenom;
        }
        return nom;
    }

    public boolean prenomObligatoire() {
        return typeStockiste == TypeStockiste.PERSONNE_PHYSIQUE;
    }

    public boolean estPersonnePhysique() {
        return typeStockiste == TypeStockiste.PERSONNE_PHYSIQUE;
    }

    public boolean estPersonneMorale() {
        return typeStockiste == TypeStockiste.PERSONNE_MORALE;
    }
}