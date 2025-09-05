package com.longrich.smartgestion.entity;

import com.longrich.smartgestion.enums.StatutEntreeStock;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entrees_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntreeStock extends BaseEntity {

    @Column(name = "numero_entree", unique = true, nullable = false)
    private String numeroEntree;

    @Column(name = "date_entree", nullable = false)
    @Builder.Default
    private LocalDateTime dateEntree = LocalDateTime.now();

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    @Column(name = "date_livraison")
    private LocalDate dateLivraison;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @Column(name = "numero_facture_fournisseur")
    private String numeroFactureFournisseur;

    @Column(name = "numero_bon_livraison")
    private String numeroBonLivraison;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutEntreeStock statut = StatutEntreeStock.EN_ATTENTE;

    @Column(name = "montant_total", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Le montant total ne peut pas être négatif")
    private BigDecimal montantTotal;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @OneToMany(mappedBy = "entreeStock", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneEntreeStock> lignesEntree = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (numeroEntree == null) {
            numeroEntree = generateNumeroEntree();
        }
    }

    private String generateNumeroEntree() {
        return "ENT-" + System.currentTimeMillis();
    }

    public void addLigneEntree(LigneEntreeStock ligne) {
        lignesEntree.add(ligne);
        ligne.setEntreeStock(this);
    }

    public void removeLigneEntree(LigneEntreeStock ligne) {
        lignesEntree.remove(ligne);
        ligne.setEntreeStock(null);
    }

    public Integer getTotalQuantite() {
        return lignesEntree.stream()
                .mapToInt(LigneEntreeStock::getQuantite)
                .sum();
    }

    public BigDecimal calculerMontantTotal() {
        return lignesEntree.stream()
                .map(ligne -> ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isValidee() {
        return statut == StatutEntreeStock.VALIDEE;
    }

    public boolean isAnnulee() {
        return statut == StatutEntreeStock.ANNULEE;
    }
}