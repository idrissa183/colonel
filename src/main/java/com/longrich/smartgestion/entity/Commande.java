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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande extends BaseEntity {

    @Column(name = "numero_commande", unique = true, nullable = false)
    private String numeroCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "La date de commande est obligatoire")
    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande;

    @Column(name = "date_livraison_prevue")
    private LocalDateTime dateLivraisonPrevue;

    @Column(name = "date_livraison_effective")
    private LocalDateTime dateLivraisonEffective;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Builder.Default
    @Column(name = "montant_total", precision = 10, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_pv", precision = 8, scale = 2)
    private BigDecimal totalPv = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tva", precision = 5, scale = 2)
    private BigDecimal tva = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "montant_ht", precision = 10, scale = 2)
    private BigDecimal montantHT = BigDecimal.ZERO;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    // Relations
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneCommande> lignes;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Facture facture;

    // Méthodes utilitaires
    @PrePersist
    public void generateNumeroCommande() {
        if (numeroCommande == null) {
            numeroCommande = "CMD-" + System.currentTimeMillis();
        }
    }

    public void calculerMontants() {
        if (lignes != null && !lignes.isEmpty()) {
            montantHT = lignes.stream()
                    .map(LigneCommande::getSousTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalPv = lignes.stream()
                    .map(ligne -> ligne.getProduit().getPv().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal montantTva = montantHT.multiply(tva).divide(BigDecimal.valueOf(100));
            montantTotal = montantHT.add(montantTva);
        }
    }

    public boolean isCommandeNonLivree() {
        return statut == StatutCommande.EN_COURS || statut == StatutCommande.EN_ATTENTE;
    }

    public int getTotalQuantiteCommandee() {
        return lignes != null ? lignes.stream()
                .mapToInt(LigneCommande::getQuantite)
                .sum() : 0;
    }

    public int getTotalQuantiteLivree() {
        return lignes != null ? lignes.stream()
                .mapToInt(LigneCommande::getQuantiteLivree)
                .sum() : 0;
    }

    public int getTotalQuantiteRestante() {
        return getTotalQuantiteCommandee() - getTotalQuantiteLivree();
    }
}
