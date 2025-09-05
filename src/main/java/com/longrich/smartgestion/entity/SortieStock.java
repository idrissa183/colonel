package com.longrich.smartgestion.entity;

import com.longrich.smartgestion.enums.TypeSortieStock;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sorties_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortieStock extends BaseEntity {

    @Column(name = "numero_sortie", unique = true, nullable = false)
    private String numeroSortie;

    @Column(name = "date_sortie", nullable = false)
    @Builder.Default
    private LocalDateTime dateSortie = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "type_sortie", nullable = false)
    @NotNull(message = "Le type de sortie est obligatoire")
    private TypeSortieStock typeSortie;

    @Column(name = "emplacement_origine", nullable = false)
    @NotNull(message = "L'emplacement d'origine est obligatoire")
    private String emplacementOrigine;

    @Column(name = "emplacement_destination")
    private String emplacementDestination;

    @Column(name = "montant_total", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @Column(name = "reference_document")
    private String referenceDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    // Pour les ventes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @OneToMany(mappedBy = "sortieStock", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneSortieStock> lignesSortie = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (numeroSortie == null) {
            numeroSortie = generateNumeroSortie();
        }
    }

    private String generateNumeroSortie() {
        String prefix = switch (typeSortie) {
            case VENTE -> "SORT-V-";
            case PERTE -> "SORT-P-";
            case TRANSFERT -> "SORT-T-";
            case PEREMPTION -> "SORT-PR-";
        };
        return prefix + System.currentTimeMillis();
    }

    public void addLigneSortie(LigneSortieStock ligne) {
        lignesSortie.add(ligne);
        ligne.setSortieStock(this);
    }

    public void removeLigneSortie(LigneSortieStock ligne) {
        lignesSortie.remove(ligne);
        ligne.setSortieStock(null);
    }

    public Integer getTotalQuantite() {
        return lignesSortie.stream()
                .mapToInt(LigneSortieStock::getQuantite)
                .sum();
    }

    public BigDecimal calculerMontantTotal() {
        return lignesSortie.stream()
                .map(ligne -> ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}