package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertes_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteStock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_alerte", nullable = false, length = 20)
    private TypeAlerte typeAlerte;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_alerte", nullable = false, length = 20)
    private NiveauAlerte niveauAlerte;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "seuil_declenche")
    private Integer seuilDeclenche;

    @Column(name = "quantite_actuelle")
    private Integer quantiteActuelle;

    @Builder.Default
    @Column(name = "est_active", nullable = false)
    private Boolean estActive = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_creation_id")
    private User utilisateurCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_resolution_id")
    private User utilisateurResolution;

    @PrePersist
    private void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    public void resoudre(User utilisateur) {
        this.estActive = false;
        this.dateResolution = LocalDateTime.now();
        this.utilisateurResolution = utilisateur;
    }

    public enum TypeAlerte {
        STOCK_FAIBLE("Stock faible"),
        RUPTURE_STOCK("Rupture de stock"),
        STOCK_NEGATIF("Stock négatif"),
        PEREMPTION_PROCHE("Péremption proche");

        private final String displayName;

        TypeAlerte(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NiveauAlerte {
        INFO("Information", "🔵"),
        ATTENTION("Attention", "🟡"),
        CRITIQUE("Critique", "🔴");

        private final String displayName;
        private final String icone;

        NiveauAlerte(String displayName, String icone) {
            this.displayName = displayName;
            this.icone = icone;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcone() {
            return icone;
        }
    }
}