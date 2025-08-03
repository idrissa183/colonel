package com.longrich.smartgestion.dto;

import com.longrich.smartgestion.enums.StatutCommande;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeDTO {

    private Long id;
    private String numeroCommande;

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;
    private String clientNom;

    @NotNull(message = "L'utilisateur est obligatoire")
    private Long userId;
    private String userNom;

    @NotNull(message = "La date de commande est obligatoire")
    private LocalDateTime dateCommande;

    private LocalDateTime dateLivraisonPrevue;
    private LocalDateTime dateLivraisonEffective;

    @NotNull(message = "Le statut est obligatoire")
    private StatutCommande statut;

    private BigDecimal montantTotal;
    private BigDecimal totalPv;
    private BigDecimal tva;
    private BigDecimal montantHT;
    private String observations;

    private List<LigneCommandeDTO> lignes;

    // Computed fields
    private Boolean commandeNonLivree;
    private Integer totalQuantiteCommandee;
    private Integer totalQuantiteLivree;
    private Integer totalQuantiteRestante;
}