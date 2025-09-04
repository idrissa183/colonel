package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.AlerteStock;
import com.longrich.smartgestion.entity.AlerteStock.TypeAlerte;
import com.longrich.smartgestion.entity.AlerteStock.NiveauAlerte;
import com.longrich.smartgestion.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlerteStockRepository extends JpaRepository<AlerteStock, Long> {

    // Alertes actives
    List<AlerteStock> findByEstActiveOrderByDateCreationDesc(Boolean estActive);

    // Comptage par statut actif
    Long countByEstActive(Boolean estActive);

    // Alertes par produit
    List<AlerteStock> findByProduitAndEstActiveOrderByDateCreationDesc(Produit produit, Boolean estActive);

    // Alertes par type et niveau
    List<AlerteStock> findByTypeAlerteAndNiveauAlerteAndEstActiveOrderByDateCreationDesc(
            TypeAlerte typeAlerte, NiveauAlerte niveauAlerte, Boolean estActive);

    // Alertes par niveau
    List<AlerteStock> findByNiveauAlerteAndEstActiveOrderByDateCreationDesc(
            NiveauAlerte niveauAlerte, Boolean estActive);

    // Page des alertes avec filtres
    @Query("SELECT a FROM AlerteStock a WHERE " +
           "(:estActive IS NULL OR a.estActive = :estActive) AND " +
           "(:typeAlerte IS NULL OR a.typeAlerte = :typeAlerte) AND " +
           "(:niveauAlerte IS NULL OR a.niveauAlerte = :niveauAlerte) " +
           "ORDER BY a.dateCreation DESC")
    Page<AlerteStock> findWithFilters(
            @Param("estActive") Boolean estActive,
            @Param("typeAlerte") TypeAlerte typeAlerte,
            @Param("niveauAlerte") NiveauAlerte niveauAlerte,
            Pageable pageable);

    // Vérifier si une alerte existe déjà pour un produit
    Optional<AlerteStock> findByProduitAndTypeAlerteAndEstActive(
            Produit produit, TypeAlerte typeAlerte, Boolean estActive);

    // Nombre d'alertes par niveau
    @Query("SELECT a.niveauAlerte, COUNT(a) FROM AlerteStock a WHERE a.estActive = true " +
           "GROUP BY a.niveauAlerte")
    List<Object[]> countAlertesByNiveau();

    // Alertes récentes (dernières 24h)
    @Query("SELECT a FROM AlerteStock a WHERE a.dateCreation >= :dateDebut AND a.estActive = true " +
           "ORDER BY a.dateCreation DESC")
    List<AlerteStock> findRecentAlertes(@Param("dateDebut") LocalDateTime dateDebut);

    // Top produits avec le plus d'alertes
    @Query("SELECT a.produit, COUNT(a) as nbAlertes FROM AlerteStock a WHERE a.estActive = true " +
           "GROUP BY a.produit ORDER BY nbAlertes DESC")
    List<Object[]> getProduitsAvecPlusDAlertes();

    // Résoudre toutes les alertes d'un produit
    @Query("UPDATE AlerteStock a SET a.estActive = false, a.dateResolution = :dateResolution, " +
           "a.utilisateurResolution = :utilisateurId WHERE a.produit = :produit AND a.estActive = true")
    void resolveAllAlertesByProduit(@Param("produit") Produit produit, 
                                   @Param("dateResolution") LocalDateTime dateResolution,
                                   @Param("utilisateurId") Long utilisateurId);

    // Alertes non résolues depuis X jours
    @Query("SELECT a FROM AlerteStock a WHERE a.estActive = true AND " +
           "a.dateCreation <= :dateLimit ORDER BY a.dateCreation ASC")
    List<AlerteStock> findAlertesNonResoluesDepuis(@Param("dateLimit") LocalDateTime dateLimit);

    // Statistiques des alertes par période
    @Query("SELECT DATE(a.dateCreation) as date, COUNT(a) as nb FROM AlerteStock a WHERE " +
           "a.dateCreation BETWEEN :dateDebut AND :dateFin " +
           "GROUP BY DATE(a.dateCreation) ORDER BY date DESC")
    List<Object[]> getStatistiquesAlertesByPeriod(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);
}