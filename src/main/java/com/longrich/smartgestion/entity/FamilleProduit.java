package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
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

    @NotBlank(message = "Le code famille est obligatoire")
    @Column(name = "code_famille", nullable = false, unique = true, length = 20)
    private String codeFamille;

    @NotBlank(message = "Le libellé famille est obligatoire")
    @Column(name = "libelle_famille", nullable = false, length = 100)
    private String libelleFamille;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "familleProduit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Produit> produits;
}
