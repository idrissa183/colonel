package com.longrich.smartgestion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteStockDTO {

    private Long id;

    private Long produitId;
    private String produitLibelle;

    private String typeAlerte;
    private String typeAlerteDisplayName;

    private String niveauAlerte;
    private String niveauAlerteDisplayName;
    private String niveauAlerteIcone;

    private String message;
    private Integer seuilDeclenche;
    private Integer quantiteActuelle;

    private Boolean estActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateResolution;

    private Long utilisateurCreationId;
    private String utilisateurCreationNom;

    private Long utilisateurResolutionId;
    private String utilisateurResolutionNom;

    // Champs calculés
    private long minutesDepuisCreation;
    private String dureeDepuisCreation;
    private boolean ancienne; // Plus de 24h

    // Méthodes utilitaires
    public boolean isCritique() {
        return "CRITIQUE".equals(niveauAlerte);
    }

    public boolean isRuptureStock() {
        return "RUPTURE_STOCK".equals(typeAlerte);
    }

    public boolean isStockFaible() {
        return "STOCK_FAIBLE".equals(typeAlerte);
    }

    public String getPriorite() {
        if (isCritique()) {
            return "Élevée";
        } else if ("ATTENTION".equals(niveauAlerte)) {
            return "Moyenne";
        }
        return "Faible";
    }

    public String getCouleurNiveau() {
        if (niveauAlerte == null) return "#6B7280";
        
        return switch (niveauAlerte) {
            case "CRITIQUE" -> "#EF4444"; // Rouge
            case "ATTENTION" -> "#F59E0B"; // Orange
            case "INFO" -> "#3B82F6"; // Bleu
            default -> "#6B7280"; // Gris
        };
    }

    public String getMessageComplet() {
        StringBuilder sb = new StringBuilder();
        
        if (produitLibelle != null) {
            sb.append(produitLibelle).append(" - ");
        }
        
        if (message != null) {
            sb.append(message);
        } else {
            // Message par défaut basé sur le type
            switch (typeAlerte != null ? typeAlerte : "") {
                case "RUPTURE_STOCK" -> sb.append("Stock épuisé");
                case "STOCK_FAIBLE" -> sb.append("Stock faible (").append(quantiteActuelle).append("/").append(seuilDeclenche).append(")");
                case "STOCK_NEGATIF" -> sb.append("Stock négatif détecté");
                case "PEREMPTION_PROCHE" -> sb.append("Péremption proche");
                default -> sb.append("Alerte de stock");
            }
        }
        
        return sb.toString();
    }
}