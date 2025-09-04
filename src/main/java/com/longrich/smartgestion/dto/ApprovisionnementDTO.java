package com.longrich.smartgestion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovisionnementDTO {

    private Long id;

    private Long produitId;
    private String produitLibelle;

    private Long fournisseurId;
    private String fournisseurNom;

    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotal;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateApprovisionnement;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReception;

    private String numeroCommande;
    private String numeroFacture;
    private String commentaire;
    private String fichierReference;

    private String statut;
    private String statutDisplayName;

    private Long utilisateurId;
    private String utilisateurNom;

    // Champs calculés
    private boolean enRetard;
    private long joursDepuisCommande;

    // Méthodes utilitaires
    public boolean isRecu() {
        return "RECU_COMPLET".equals(statut) || "RECU_PARTIEL".equals(statut);
    }

    public boolean isPending() {
        return "EN_ATTENTE".equals(statut) || "COMMANDE".equals(statut);
    }

    public boolean isComplete() {
        return "RECU_COMPLET".equals(statut);
    }

    public String getStatutColor() {
        if (statut == null) return "#6B7280"; // Gris
        
        return switch (statut) {
            case "EN_ATTENTE" -> "#F59E0B"; // Orange
            case "COMMANDE" -> "#3B82F6"; // Bleu
            case "RECU_PARTIEL" -> "#F59E0B"; // Orange
            case "RECU_COMPLET" -> "#10B981"; // Vert
            case "ANNULE" -> "#EF4444"; // Rouge
            default -> "#6B7280"; // Gris
        };
    }

    public String getStatutIcon() {
        if (statut == null) return "❓";
        
        return switch (statut) {
            case "EN_ATTENTE" -> "⏳";
            case "COMMANDE" -> "📦";
            case "RECU_PARTIEL" -> "📥";
            case "RECU_COMPLET" -> "✅";
            case "ANNULE" -> "❌";
            default -> "❓";
        };
    }
}