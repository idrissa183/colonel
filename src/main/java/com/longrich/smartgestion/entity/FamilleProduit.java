package com.longrich.smartgestion.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "famille_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilleProduit extends BaseEntity {

    @NotBlank(message = "Le libellé famille est obligatoire")
    @Column(name = "libelle_famille", nullable = false, length = 100)
    private String libelleFamille;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "familleProduit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Produit> produits;
}
