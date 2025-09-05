package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.CommandeFournisseur;
import com.longrich.smartgestion.entity.Fournisseur;
import com.longrich.smartgestion.enums.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.numeroCommande = :numeroCommande")
    Optional<CommandeFournisseur> findByNumeroCommande(@Param("numeroCommande") String numeroCommande);

    @Query("SELECT DISTINCT c FROM CommandeFournisseur c " +
           "LEFT JOIN FETCH c.lignes l " +
           "LEFT JOIN FETCH l.produit " +
           "JOIN FETCH c.fournisseur " +
           "WHERE c.numeroCommande = :numeroCommande " +
           "AND (l.active = true OR l IS NULL)")
    Optional<CommandeFournisseur> findByNumeroCommandeWithLignes(@Param("numeroCommande") String numeroCommande);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.fournisseur = :fournisseur")
    List<CommandeFournisseur> findByFournisseur(@Param("fournisseur") Fournisseur fournisseur);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.statut = :statut")
    List<CommandeFournisseur> findByStatut(@Param("statut") StatutCommande statut);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.fournisseur = :fournisseur AND c.statut = :statut")
    List<CommandeFournisseur> findByFournisseurAndStatut(@Param("fournisseur") Fournisseur fournisseur, @Param("statut") StatutCommande statut);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.dateCommande BETWEEN :debut AND :fin")
    List<CommandeFournisseur> findByDateCommandeBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.dateCommande BETWEEN :debut AND :fin ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findByDateCommandeBetweenOrderByDateCommandeDesc(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.active = true AND " +
           "(LOWER(c.numeroCommande) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.fournisseur.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.fournisseur.prenom) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<CommandeFournisseur> searchCommandes(@Param("search") String search);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.active = true AND c.statut = :statut")
    List<CommandeFournisseur> findActiveByStatut(@Param("statut") StatutCommande statut);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.active = true ORDER BY c.dateCommande DESC")
    List<CommandeFournisseur> findAllActiveOrderByDateDesc();

    Page<CommandeFournisseur> findByActiveTrue(Pageable pageable);

    @Query("SELECT COUNT(c) FROM CommandeFournisseur c WHERE c.active = true AND c.statut = :statut")
    long countByStatut(@Param("statut") StatutCommande statut);

    @Query("SELECT SUM(c.montantTotal) FROM CommandeFournisseur c WHERE c.active = true AND c.statut IN :statuts")
    BigDecimal sumMontantTotalByStatutIn(@Param("statuts") List<StatutCommande> statuts);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.active = true AND " +
           "c.dateLivraisonPrevue IS NOT NULL AND c.dateLivraisonPrevue < :date AND " +
           "c.statut NOT IN ('LIVREE', 'ANNULEE')")
    List<CommandeFournisseur> findCommandesEnRetard(@Param("date") LocalDateTime date);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.active = true AND " +
           "c.fournisseur.id = :fournisseurId AND c.statut = :statut")
    List<CommandeFournisseur> findByFournisseurIdAndStatut(
            @Param("fournisseurId") Long fournisseurId, 
            @Param("statut") StatutCommande statut);

    @Query("SELECT c FROM CommandeFournisseur c JOIN FETCH c.fournisseur WHERE c.active = true AND " +
           "c.dateCommande BETWEEN :debut AND :fin AND c.statut = :statut")
    List<CommandeFournisseur> findByPeriodAndStatut(
            @Param("debut") LocalDateTime debut, 
            @Param("fin") LocalDateTime fin, 
            @Param("statut") StatutCommande statut);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.numeroCommande, 6) AS integer)), 0) " +
           "FROM CommandeFournisseur c WHERE c.numeroCommande LIKE 'CMDF-%'")
    Integer findLastCommandeNumber();
}