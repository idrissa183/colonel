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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Pattern(regexp = "^([A-Z]{2}\\d{8})?$", message = "Le code partenaire doit respecter le format ISO2 suivi de 8 chiffres (ex: BF12345678)")
    @Column(name = "code_partenaire", unique = true)
    private String codePartenaire;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "province_id")
    private Province province;

    @Pattern(regexp = "^(B\\d{8})?$", message = "Le numéro de la CNIB doit respecter le format B suivi de 8 chiffres")
    @Column(name = "cnib", length = 20)
    private String cnib;

    @Pattern(regexp = "^(\\+226[02567]\\d{7}|[02567]\\d{7})?$", message = "Numéro de téléphone burkinabè invalide")
    @Column(name = "telephone")
    private String telephone;

    @Email(message = "Format d'email invalide")
    @Column(name = "email")
    private String email;

    @NotNull(message = "Le type de client est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_client", nullable = false)
    private TypeClient typeClient;

    @Column(name = "adresse")
    private String adresse;

    @Builder.Default
    @Column(name = "total_pv")
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
