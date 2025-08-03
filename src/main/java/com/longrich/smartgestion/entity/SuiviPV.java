package com.longrich.smartgestion.entity;

import com.longrich.smartgestion.enums.TypeVente;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suivi_pv")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuiviPV extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @NotNull(message = "La quantité est obligatoire")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @NotNull(message = "Le nombre de PV est obligatoire")
    @Column(name = "pv_gagne", precision = 8, scale = 2, nullable = false)
    private BigDecimal pvGagne;

    @NotNull(message = "Le montant de la vente est obligatoire")
    @Column(name = "montant_vente", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantVente;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_vente", nullable = false)
    private TypeVente typeVente;

    @Column(name = "date_vente", nullable = false)
    private LocalDate dateVente;

    @Column(name = "numero_facture")
    private String numeroFacture;

    @Column(name = "observation")
    private String observation;

    @PrePersist
    private void prePersist() {
        if (dateVente == null) {
            dateVente = LocalDate.now();
        }
    }
}