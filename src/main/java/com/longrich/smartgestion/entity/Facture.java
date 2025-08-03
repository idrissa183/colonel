package com.longrich.smartgestion.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "factures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture extends BaseEntity {

    @Column(name = "numero_facture", unique = true, nullable = false)
    private String numeroFacture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull(message = "La date de facturation est obligatoire")
    @Column(name = "date_facturation", nullable = false)
    private LocalDateTime dateFacturation;

    @Column(name = "date_echeance")
    private LocalDateTime dateEcheance;

    @Column(name = "montant_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantTotal;

    @Builder.Default
    @Column(name = "montant_paye", precision = 10, scale = 2)
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(name = "montant_restant", precision = 10, scale = 2)
    private BigDecimal montantRestant;

    @Builder.Default
    @Column(name = "payee", nullable = false)
    private Boolean payee = false;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    // Méthodes utilitaires
    @PrePersist
    public void prePersist() {
        if (numeroFacture == null) {
            numeroFacture = "FACT-" + System.currentTimeMillis();
        }
        calculerMontantRestant();
    }

    @PreUpdate
    public void preUpdate() {
        calculerMontantRestant();
    }

    private void calculerMontantRestant() {
        if (montantTotal != null && montantPaye != null) {
            montantRestant = montantTotal.subtract(montantPaye);
            payee = montantRestant.compareTo(BigDecimal.ZERO) <= 0;
        }
    }
}
