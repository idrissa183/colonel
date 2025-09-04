package com.longrich.smartgestion.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesStockDTO {

    // Statistiques générales
    private Long totalProduits;
    private Long totalProduitsEnStock;
    private Long totalProduitsRupture;
    private Long totalProduitsStockFaible;

    private BigDecimal valeurTotaleStock;
    private BigDecimal valeurMoyenneStock;

    // Mouvements du jour
    private Long mouvementsAujourdhui;
    private Long entreesAujourdhui;
    private Long sortiesAujourdhui;

    // Alertes
    private Long totalAlertes;
    private Long alertesCritiques;
    private Long alertesAttention;
    private Long alertesInfo;

    // Approvisionnements
    private Long approvisionnementsPendants;
    private Long approvisionnementsDuJour;
    private BigDecimal valeurApprovisionnementsPendants;

    // Ajustements
    private Long ajustementsEnAttente;
    private Long ajustementsDuJour;

    // Top produits
    private Map<String, Object> topProduitsVendus;
    private Map<String, Object> topProduitsStockFaible;
    private Map<String, Object> topProduitsAlertes;

    // Tendances (évolution par rapport à la période précédente)
    private Double tendanceStock; // en pourcentage
    private Double tendanceMouvements;
    private Double tendanceAlertes;

    // Données pour graphiques
    private Map<String, Long> mouvementsParJour;
    private Map<String, Long> alertesParNiveau;
    private Map<String, BigDecimal> valeurStockParCategorie;

    // Ratios et indicateurs
    private Double tauxRotationStock;
    private Double tauxCouvertureStock; // en jours
    private Double pourcentageStockFaible;
    private Double pourcentageRupture;

    // Méthodes utilitaires
    public String getTendanceStockTexte() {
        if (tendanceStock == null) return "Stable";
        
        if (tendanceStock > 5) return "Forte hausse";
        if (tendanceStock > 0) return "Hausse";
        if (tendanceStock < -5) return "Forte baisse";
        if (tendanceStock < 0) return "Baisse";
        return "Stable";
    }

    public String getCouleurTendanceStock() {
        if (tendanceStock == null) return "#6B7280";
        
        if (tendanceStock > 0) return "#10B981"; // Vert pour hausse
        if (tendanceStock < 0) return "#EF4444"; // Rouge pour baisse
        return "#6B7280"; // Gris pour stable
    }

    public String getIconeTendanceStock() {
        if (tendanceStock == null) return "➡️";
        
        if (tendanceStock > 5) return "📈";
        if (tendanceStock > 0) return "⬆️";
        if (tendanceStock < -5) return "📉";
        if (tendanceStock < 0) return "⬇️";
        return "➡️";
    }

    public boolean hasStockCritique() {
        return totalProduitsRupture != null && totalProduitsRupture > 0;
    }

    public boolean hasAlertesCritiques() {
        return alertesCritiques != null && alertesCritiques > 0;
    }

    public String getStatutGlobal() {
        if (hasStockCritique() || hasAlertesCritiques()) {
            return "Critique";
        }
        
        if ((totalProduitsStockFaible != null && totalProduitsStockFaible > 0) ||
            (alertesAttention != null && alertesAttention > 0)) {
            return "Attention";
        }
        
        return "Normal";
    }

    public String getCouleurStatutGlobal() {
        return switch (getStatutGlobal()) {
            case "Critique" -> "#EF4444";
            case "Attention" -> "#F59E0B";
            default -> "#10B981";
        };
    }
}