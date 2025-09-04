package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.AjustementInventaire;
import com.longrich.smartgestion.entity.AjustementInventaire.MotifAjustement;
import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AjustementInventaireRepository extends JpaRepository<AjustementInventaire, Long> {

    // Ajustements par produit
    List<AjustementInventaire> findByProduitOrderByDateAjustementDesc(Produit produit);

    // Ajustements par motif
    List<AjustementInventaire> findByMotifAjustementOrderByDateAjustementDesc(MotifAjustement motifAjustement);

    // Ajustements par statut de validation
    List<AjustementInventaire> findByValideOrderByDateAjustementDesc(Boolean valide);

    // Ajustements par période
    List<AjustementInventaire> findByDateAjustementBetweenOrderByDateAjustementDesc(
            LocalDate dateDebut, LocalDate dateFin);

    // Page des ajustements avec filtres
    @Query("SELECT a FROM AjustementInventaire a WHERE " +
           "(:valide IS NULL OR a.valide = :valide) AND " +
           "(:motif IS NULL OR a.motifAjustement = :motif) AND " +
           "(:dateDebut IS NULL OR a.dateAjustement >= :dateDebut) AND " +
           "(:dateFin IS NULL OR a.dateAjustement <= :dateFin) " +
           "ORDER BY a.dateAjustement DESC")
    Page<AjustementInventaire> findWithFilters(
            @Param("valide") Boolean valide,
            @Param("motif") MotifAjustement motif,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            Pageable pageable);

    // Ajustements avec écarts importants
    @Query("SELECT a FROM AjustementInventaire a WHERE ABS(a.ecart) >= :seuilEcart " +
           "ORDER BY ABS(a.ecart) DESC")
    List<AjustementInventaire> findWithSignificantVariance(@Param("seuilEcart") Integer seuilEcart);

    // Ajustements excédentaires
    @Query("SELECT a FROM AjustementInventaire a WHERE a.ecart > 0 " +
           "ORDER BY a.ecart DESC")
    List<AjustementInventaire> findExcedentaires();

    // Ajustements déficitaires
    @Query("SELECT a FROM AjustementInventaire a WHERE a.ecart < 0 " +
           "ORDER BY a.ecart ASC")
    List<AjustementInventaire> findDeficitaires();

    // Statistiques des ajustements
    @Query("SELECT COUNT(a) FROM AjustementInventaire a WHERE a.dateAjustement = :date")
    Long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(ABS(a.ecart)), 0) FROM AjustementInventaire a WHERE " +
           "a.dateAjustement BETWEEN :dateDebut AND :dateFin")
    Long getTotalEcartAbsoluByPeriod(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    // Ajustements en attente de validation
    @Query("SELECT a FROM AjustementInventaire a WHERE a.valide = false " +
           "ORDER BY a.dateAjustement ASC")
    List<AjustementInventaire> findPendingValidation();

    // Historique des ajustements d'un produit
    @Query("SELECT a FROM AjustementInventaire a WHERE a.produit.id = :produitId " +
           "ORDER BY a.dateAjustement DESC")
    List<AjustementInventaire> findHistoriqueByProduitId(@Param("produitId") Long produitId);

    // Produits avec ajustements fréquents
    @Query("SELECT a.produit, COUNT(a) as nbAjustements FROM AjustementInventaire a WHERE " +
           "a.dateAjustement >= :dateDebut " +
           "GROUP BY a.produit HAVING COUNT(a) >= :seuilFrequence " +
           "ORDER BY nbAjustements DESC")
    List<Object[]> findProduitsAvecAjustementsFrequents(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("seuilFrequence") Long seuilFrequence);

    // Statistiques par motif
    @Query("SELECT a.motifAjustement, COUNT(a) as nb, COALESCE(SUM(ABS(a.ecart)), 0) as totalEcart " +
           "FROM AjustementInventaire a WHERE a.dateAjustement BETWEEN :dateDebut AND :dateFin " +
           "GROUP BY a.motifAjustement ORDER BY nb DESC")
    List<Object[]> getStatistiquesByMotif(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    // Tendances des écarts par mois
    @Query("SELECT YEAR(a.dateAjustement) as annee, MONTH(a.dateAjustement) as mois, " +
           "COUNT(a) as nbAjustements, COALESCE(AVG(CAST(ABS(a.ecart) AS double)), 0) as ecartMoyen " +
           "FROM AjustementInventaire a " +
           "GROUP BY YEAR(a.dateAjustement), MONTH(a.dateAjustement) " +
           "ORDER BY annee DESC, mois DESC")
    List<Object[]> getTendancesEcarts();
}