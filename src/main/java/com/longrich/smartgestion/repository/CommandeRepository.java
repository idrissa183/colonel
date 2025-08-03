package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Commande;
import com.longrich.smartgestion.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    Optional<Commande> findByNumeroCommande(String numeroCommande);

    List<Commande> findByClientId(Long clientId);

    List<Commande> findByUserId(Long userId);

    List<Commande> findByStatut(StatutCommande statut);

    List<Commande> findByStatutIn(List<StatutCommande> statuts);

    @Query("SELECT c FROM Commande c WHERE c.dateCommande BETWEEN :debut AND :fin")
    List<Commande> findByDateCommandeBetween(@Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM Commande c WHERE c.statut IN ('EN_COURS', 'EN_ATTENTE', 'PARTIELLEMENT_LIVREE')")
    List<Commande> findCommandesNonLivrees();

    @Query("SELECT SUM(c.montantTotal) FROM Commande c WHERE c.statut = 'LIVREE' AND c.dateCommande BETWEEN :debut AND :fin")
    Double getTotalVentesPeriod(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Commande c WHERE c.statut IN ('EN_COURS', 'EN_ATTENTE', 'PARTIELLEMENT_LIVREE')")
    Long countCommandesNonLivrees();
}