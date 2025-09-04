package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "vente_promotionnelle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentePromotionnelle extends BaseEntity {

    @NotNull(message = "Le nom de la promotion est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "ventePromotionnelle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProduitPromotionnel> produitsPromotionnels;

    public boolean isActive() {
        if (!active) return false;
        LocalDate now = LocalDate.now();
        return !now.isBefore(dateDebut) && !now.isAfter(dateFin);
    }
}