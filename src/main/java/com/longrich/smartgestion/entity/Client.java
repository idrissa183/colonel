package com.longrich.smartgestion.entity;

import java.util.List;

import com.longrich.smartgestion.enums.TypeClient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @NotBlank(message = "Le code est obligatoire")
    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "cnib", length = 20)
    private String cnib;

    @Column(name = "telephone")
    private String telephone;

    @Email(message = "Format d'email invalide")
    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_client", nullable = false)
    private TypeClient typeClient;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "localisation")
    private String localisation;

    @Column(name = "code_parrain", length = 20)
    private String codeParrain;

    @Column(name = "code_placement", length = 20)
    private String codePlacement;

    @Builder.Default
    @Column(name = "total_pv", nullable = false)
    private Integer totalPv = 0;

    @Builder.Default
    @Column(name = "code_definitif", nullable = false)
    private Boolean codeDefinitif = false;

    // Relations
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Commande> commandes;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Facture> factures;

    // Méthodes utilitaires
    public String getNomComplet() {
        return nom + " " + prenom;
    }

    public String getClientId() {
        return code + " - " + getNomComplet() + " " + telephone + " " + (localisation != null ? localisation : "");
    }

    public boolean peutDeveniPartenaire() {
        return typeClient == TypeClient.EN_ATTENTE_PARTENAIRE && totalPv >= 50000;
    }
}
