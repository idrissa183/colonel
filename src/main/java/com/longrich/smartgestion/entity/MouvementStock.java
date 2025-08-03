package com.longrich.smartgestion.entity;

import com.longrich.smartgestion.enums.TypeMouvement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false)
    private TypeMouvement typeMouvement;

    @NotNull(message = "La quantité est obligatoire")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @Column(name = "origine")
    private String origine;

    @Column(name = "destination")
    private String destination;

    @Column(name = "reference_document")
    private String referenceDocument;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private User utilisateur;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @PrePersist
    private void prePersist() {
        if (dateMouvement == null) {
            dateMouvement = LocalDateTime.now();
        }
    }
}