package com.longrich.smartgestion.entity;

import java.util.List;
import java.util.Random;

import com.longrich.smartgestion.enums.TypeClient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Pattern(regexp = "^BF\\d{8}$", message = "Le code partenaire doit respecter le format BF suivi de 8 chiffres")
    @Column(name = "code_partenaire", unique = true)
    private String codePartenaire;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "province_id")
    private Province province;


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
        String clientId = id + " - " + getNomComplet();
        if (telephone != null && !telephone.isEmpty()) {
            clientId += " " + telephone;
        }
        return clientId;
    }

    public boolean peutDeveniPartenaire() {
        return typeClient == TypeClient.EN_ATTENTE_PARTENAIRE && totalPv >= 50000;
    }

    public void genererCodePartenaire() {
        if (this.typeClient == TypeClient.PARTENAIRE && this.codePartenaire == null) {
            Random random = new Random();
            String numeroAleatoire = String.format("%08d", random.nextInt(100000000));
            this.codePartenaire = "BF" + numeroAleatoire;
        }
    }

    public boolean estPartenaire() {
        return typeClient == TypeClient.PARTENAIRE;
    }
}
