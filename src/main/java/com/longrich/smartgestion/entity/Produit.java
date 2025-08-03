package com.longrich.smartgestion.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit extends BaseEntity {

    @NotBlank(message = "Le code barre est obligatoire")
    @Column(name = "code_barre", unique = true, nullable = false)
    private String codeBarre;

    @NotBlank(message = "Le libellé est obligatoire")
    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    @NotNull(message = "Le prix d'achat est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix d'achat doit être positif")
    @Column(name = "prix_achat", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixAchat;

    @NotNull(message = "Le prix de revente est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix de revente doit être positif")
    @Column(name = "prix_revente", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixRevente;

    @NotNull(message = "Le nombre de PV est obligatoire")
    @DecimalMin(value = "0.0", message = "Le nombre de PV doit être positif ou nul")
    @Column(name = "pv", precision = 5, scale = 2, nullable = false)
    private BigDecimal pv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "famille_produit_id")
    private FamilleProduit familleProduit;

    @Builder.Default
    @Column(name = "stock_minimum")
    private Integer stockMinimum = 0;

    @Builder.Default
    @Column(name = "seuil_alerte")
    private Integer seuilAlerte = 5;

    // Relations
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Stock> stocks;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneCommande> lignesCommande;

    // Méthodes utilitaires
    public BigDecimal getMarge() {
        return prixRevente.subtract(prixAchat);
    }

    public BigDecimal getPourcentageMarge() {
        if (prixAchat.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getMarge().divide(prixAchat, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public String getProductDisplay() {
        return codeBarre + " - " + libelle + " - " + prixRevente + "F - " + pv + " PV";
    }

    public boolean isStockCritique() {
        return getCurrentStock() <= seuilAlerte;
    }

    public int getCurrentStock() {
        return stocks != null ? stocks.stream()
                .mapToInt(Stock::getQuantite)
                .sum() : 0;
    }
}