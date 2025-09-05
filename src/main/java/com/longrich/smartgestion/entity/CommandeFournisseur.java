package com.longrich.smartgestion.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.longrich.smartgestion.enums.StatutCommande;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "commandes_fournisseur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeFournisseur extends BaseEntity {

    @Column(name = "numero_commande", unique = true, nullable = false)
    private String numeroCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @NotNull(message = "La date de commande est obligatoire")
    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande;

    @Column(name = "date_livraison_prevue")
    private LocalDateTime dateLivraisonPrevue;

    @Column(name = "date_livraison_reelle")
    private LocalDateTime dateLivraisonReelle;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Builder.Default
    @Column(name = "montant_total", precision = 10, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    // Relations
    @OneToMany(mappedBy = "commandeFournisseur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneCommandeFournisseur> lignes;

    @PrePersist
    public void generateNumeroCommande() {
        if (numeroCommande == null) {
            numeroCommande = "CMDF-" + System.currentTimeMillis();
        }
    }

    // Méthodes pour gestion automatique du statut selon les livraisons
    public StatutCommande calculerStatutSelonLivraisons() {
        if (lignes == null || lignes.isEmpty()) {
            return StatutCommande.EN_COURS;
        }

        boolean touteQuantitesLivrees = lignes.stream()
            .allMatch(ligne -> ligne.getQuantiteLivree().equals(ligne.getQuantiteCommandee()));

        boolean aucuneQuantiteLivree = lignes.stream()
            .allMatch(ligne -> ligne.getQuantiteLivree() == 0);

        if (touteQuantitesLivrees) {
            return StatutCommande.LIVREE;
        } else if (aucuneQuantiteLivree) {
            return this.statut; // Garder le statut actuel si rien n'est livré
        } else {
            return StatutCommande.PARTIELLEMENT_LIVREE;
        }
    }

    public int getTotalQuantiteCommandee() {
        if (lignes == null) return 0;
        return lignes.stream().mapToInt(ligne -> ligne.getQuantiteCommandee()).sum();
    }

    public int getTotalQuantiteLivree() {
        if (lignes == null) return 0;
        return lignes.stream().mapToInt(ligne -> ligne.getQuantiteLivree()).sum();
    }

    public double getPourcentageGlobalLivraison() {
        int totalCommande = getTotalQuantiteCommandee();
        if (totalCommande == 0) return 0.0;
        return (getTotalQuantiteLivree() * 100.0) / totalCommande;
    }
}
