package com.longrich.smartgestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "salle_vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalleVente extends BaseEntity {

    @NotBlank(message = "Le code salle est obligatoire")
    @Size(max = 20, message = "Le code salle ne peut dépasser 20 caractères")
    @Column(name = "code_salle", nullable = false, unique = true, length = 20)
    private String codeSalle;

    @NotBlank(message = "Le nom de la salle est obligatoire")
    @Size(max = 100, message = "Le nom de la salle ne peut dépasser 100 caractères")
    @Column(name = "nom_salle", nullable = false, length = 100)
    private String nomSalle;

    @Size(max = 200, message = "La localisation ne peut dépasser 200 caractères")
    @Column(name = "localisation", length = 200)
    private String localisation;

    @Size(max = 500, message = "La description ne peut dépasser 500 caractères")
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private User responsable;

    @OneToMany(mappedBy = "salleVente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Stock> stocks;
}