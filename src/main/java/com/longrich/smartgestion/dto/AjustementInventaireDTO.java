package com.longrich.smartgestion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjustementInventaireDTO {

    private Long id;

    private Long produitId;
    private String produitLibelle;

    private Integer quantiteTheorique;
    private Integer quantitePhysique;
    private Integer ecart;

    private String motifAjustement;
    private String motifAjustementDisplayName;

    private String commentaire;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateAjustement;

    private Long utilisateurId;
    private String utilisateurNom;

    private String documentJustificatif;

    private Boolean valide;

    private Long validateurId;
    private String validateurNom;

    // Champs calculés
    private String typeEcart; // EXCEDENTAIRE, DEFICITAIRE, CONFORME
    private String pourcentageEcart;
    private String impactFinancier;

    // Méthodes utilitaires
    public boolean estExcedentaire() {
        return ecart != null && ecart > 0;
    }

    public boolean estDeficitaire() {
        return ecart != null && ecart < 0;
    }

    public boolean estConforme() {
        return ecart != null && ecart == 0;
    }

    public String getTypeEcartText() {
        if (ecart == null) return "Inconnu";
        
        if (ecart > 0) return "Excédentaire";
        if (ecart < 0) return "Déficitaire";
        return "Conforme";
    }

    public String getCouleurEcart() {
        if (ecart == null) return "#6B7280";
        
        if (ecart > 0) return "#10B981"; // Vert pour excédent
        if (ecart < 0) return "#EF4444"; // Rouge pour déficit
        return "#6B7280"; // Gris pour conforme
    }

    public String getIconeEcart() {
        if (ecart == null) return "❓";
        
        if (ecart > 0) return "📈"; // Flèche montante
        if (ecart < 0) return "📉"; // Flèche descendante
        return "✅"; // Check pour conforme
    }

    public String getEcartFormate() {
        if (ecart == null) return "0";
        
        if (ecart > 0) return "+" + ecart;
        return ecart.toString();
    }

    public String getPourcentageEcartCalcule() {
        if (quantiteTheorique == null || quantiteTheorique == 0 || ecart == null) {
            return "0%";
        }
        
        double pourcentage = (Math.abs(ecart.doubleValue()) / quantiteTheorique.doubleValue()) * 100;
        return String.format("%.1f%%", pourcentage);
    }

    public String getSeverite() {
        if (quantiteTheorique == null || quantiteTheorique == 0 || ecart == null) {
            return "Faible";
        }
        
        double pourcentage = (Math.abs(ecart.doubleValue()) / quantiteTheorique.doubleValue()) * 100;
        
        if (pourcentage >= 20) return "Critique";
        if (pourcentage >= 10) return "Élevée";
        if (pourcentage >= 5) return "Moyenne";
        return "Faible";
    }

    public String getStatutValidation() {
        if (valide == null) return "En attente";
        return valide ? "Validé" : "En attente";
    }

    public String getCouleurValidation() {
        if (valide == null || !valide) return "#F59E0B"; // Orange pour en attente
        return "#10B981"; // Vert pour validé
    }
}