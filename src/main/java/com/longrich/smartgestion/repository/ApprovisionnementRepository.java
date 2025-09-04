package com.longrich.smartgestion.repository;

import com.longrich.smartgestion.entity.Approvisionnement;
import com.longrich.smartgestion.entity.Approvisionnement.StatutApprovisionnement;
import com.longrich.smartgestion.entity.Produit;
import com.longrich.smartgestion.entity.Fournisseur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ApprovisionnementRepository extends JpaRepository<Approvisionnement, Long> {

    // Recherche par produit
    List<Approvisionnement> findByProduitOrderByDateApprovisionnementDesc(Produit produit);

    // Recherche par fournisseur
    List<Approvisionnement> findByFournisseurOrderByDateApprovisionnementDesc(Fournisseur fournisseur);

    // Recherche par statut
    List<Approvisionnement> findByStatutOrderByDateApprovisionnementDesc(StatutApprovisionnement statut);

    // Recherche par période
    List<Approvisionnement> findByDateApprovisionnementBetweenOrderByDateApprovisionnementDesc(
            LocalDate dateDebut, LocalDate dateFin);

    // Page des approvisionnements avec filtres
    @Query("SELECT a FROM Approvisionnement a WHERE " +
           "(:statut IS NULL OR a.statut = :statut) AND " +
           "(:dateDebut IS NULL OR a.dateApprovisionnement >= :dateDebut) AND " +
           "(:dateFin IS NULL OR a.dateApprovisionnement <= :dateFin) " +
           "ORDER BY a.dateApprovisionnement DESC")
    Page<Approvisionnement> findWithFilters(
            @Param("statut") StatutApprovisionnement statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            Pageable pageable);

    // Statistiques des approvisionnements
    @Query("SELECT COUNT(a) FROM Approvisionnement a WHERE a.dateApprovisionnement = :date")
    Long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(a.prixTotal), 0) FROM Approvisionnement a WHERE " +
           "a.dateApprovisionnement BETWEEN :dateDebut AND :dateFin AND " +
           "a.statut = :statut")
    BigDecimal getTotalValueByPeriodAndStatut(
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("statut") StatutApprovisionnement statut);

    // Approvisionnements en attente
    @Query("SELECT a FROM Approvisionnement a WHERE a.statut IN :statuts ORDER BY a.dateApprovisionnement ASC")
    List<Approvisionnement> findPendingApprovisionnements(@Param("statuts") List<StatutApprovisionnement> statuts);

    // Recherche par numéro de commande ou facture
    List<Approvisionnement> findByNumeroCommandeContainingIgnoreCaseOrNumeroFactureContainingIgnoreCase(
            String numeroCommande, String numeroFacture);

    // Historique des approvisionnements d'un produit
    @Query("SELECT a FROM Approvisionnement a WHERE a.produit.id = :produitId " +
           "ORDER BY a.dateApprovisionnement DESC")
    List<Approvisionnement> findHistoriqueByProduitId(@Param("produitId") Long produitId);

    // Top fournisseurs par volume
    @Query("SELECT a.fournisseur, COUNT(a) as nb, COALESCE(SUM(a.quantite), 0) as totalQuantite " +
           "FROM Approvisionnement a WHERE a.statut = 'RECU_COMPLET' " +
           "GROUP BY a.fournisseur ORDER BY totalQuantite DESC")
    List<Object[]> getTopFournisseursByVolume();
}