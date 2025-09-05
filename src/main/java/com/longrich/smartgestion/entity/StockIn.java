package com.longrich.smartgestion.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_ins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @NotNull(message = "Le produit est obligatoire")
    private Produit produit;

    @Column(name = "quantite", nullable = false)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins de 1")
    private Integer quantite;

    @Column(name = "date_entree", nullable = false)
    @Builder.Default
    private LocalDateTime dateEntree = LocalDateTime.now(); // Date par défaut à l'heure actuelle

    @Column(name = "motif_entree", columnDefinition = "TEXT")
    private String motifEntree; // Ex: "Achat fournisseur", "Retour client", "Inventaire"

    // Si vous avez une gestion des utilisateurs
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "utilisateur_id")
    // private User utilisateur;
}